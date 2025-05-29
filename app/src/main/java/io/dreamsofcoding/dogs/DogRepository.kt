package io.dreamsofcoding.dogs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import io.dreamsofcoding.dogs.local.EntityMapper.toDomainImageList
import io.dreamsofcoding.dogs.local.EntityMapper.toDomainList
import io.dreamsofcoding.dogs.local.EntityMapper.toEntityList
import io.dreamsofcoding.dogs.local.EntityMapper.toImageEntityList
import io.dreamsofcoding.dogs.local.DogBreedDao
import io.dreamsofcoding.dogs.local.DogDatabase
import io.dreamsofcoding.dogs.local.DogImageDao
import io.dreamsofcoding.dogs.model.ApiResult
import io.dreamsofcoding.dogs.model.DogBreed
import io.dreamsofcoding.dogs.model.DogImage
import io.dreamsofcoding.dogs.model.ImagesResponse
import io.dreamsofcoding.dogs.model.ListResponse
import io.dreamsofcoding.dogs.remote.DogService
import io.dreamsofcoding.dogs.remote.DogsException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DogRepository @Inject constructor(
    private val service: DogService,
    private val breedDao: DogBreedDao,
    private val imageDao: DogImageDao,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) {

    /**
     * Fetches all available dog breeds
     * @return ApiResult containing list of dog breeds or error
     */
    suspend fun getBreeds(): ApiResult<List<DogBreed>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Fetching dog breeds")

            val cachedBreeds = breedDao.getAllBreeds()
            val cacheExpiry = System.currentTimeMillis() - DogDatabase.CACHE_DURATION_BREEDS

            if (cachedBreeds.isNotEmpty() && cachedBreeds.first().cachedAt > cacheExpiry) {
                Timber.d("Returning ${cachedBreeds.size} breeds from cache")
                return@withContext ApiResult.Success(cachedBreeds.toDomainList())
            }

            Timber.d("Fetching breeds from API")
            val response = service.getBreeds()

            if (response.status != "success") {
                Timber.w("API returned non-success status: ${response.status}")
                if (cachedBreeds.isNotEmpty()) {
                    Timber.d("API failed, returning stale cache")
                    return@withContext ApiResult.Success(cachedBreeds.toDomainList())
                }
                return@withContext ApiResult.Error(
                    DogsException.ApiException(
                        code = 0,
                        message = "API returned status: ${response.status}"
                    )
                )
            }

            val breeds = mapBreeds(response)
            val entities = breeds.toEntityList()

            breedDao.clearAll()
            breedDao.insertBreeds(entities)

            Timber.d("Successfully fetched and cached ${breeds.size} dog breeds")
            ApiResult.Success(breeds)

        } catch (e: Exception) {
            Timber.e(e, "Error fetching dog breeds")

            val cachedBreeds = breedDao.getAllBreeds()
            if (cachedBreeds.isNotEmpty()) {
                Timber.d("API failed, returning cached breeds")
                return@withContext ApiResult.Success(cachedBreeds.toDomainList())
            }

            ApiResult.Error(mapException(e))
        }
    }

    private fun mapBreeds(response: ListResponse): List<DogBreed> {
        return response.message.map { (breedName, subBreeds) ->
            DogBreed(
                name = breedName,
                subBreeds = subBreeds
            )
        }
    }

    suspend fun getBreedImages(
        breed: String,
        count: Int? = null
    ): ApiResult<List<DogImage>> = withContext(Dispatchers.IO) {
        try {
            if (breed.isBlank()) {
                return@withContext ApiResult.Error(
                    DogsException.InvalidBreedException("Breed name cannot be empty")
                )
            }
            val normalized = breed.lowercase().trim()
            Timber.d("Loading cached images for $normalized")
            val cached = imageDao.getImagesByBreed(normalized)
            val expiry = System.currentTimeMillis() - DogDatabase.CACHE_DURATION_IMAGES

            if (cached.isNotEmpty()
                && cached.first().cachedAt > expiry
                && (cached.size > 1)
            ) {
                Timber.d("Returning ${cached.size} cached images")
                return@withContext ApiResult.Success(
                    cached.take(count ?: cached.size).toDomainImageList()
                )
            }

            Timber.d("Fetching images from API for $normalized")
            val response = if (count != null)
                service.getBreedImages(normalized, count)
            else
                service.getAllBreedImages(normalized)

            if (response.status != "success") {
                Timber.w("API error status=${response.status}")
                if (cached.isNotEmpty()) {
                    return@withContext ApiResult.Success(
                        cached.take(count ?: cached.size).toDomainImageList()
                    )
                }
                return@withContext ApiResult.Error(
                    DogsException.ApiException(0, "API returned status ${response.status}")
                )
            }

            val images = mapImages(response, normalized)
            imageDao.clearImagesForBreed(normalized)
            imageDao.insertImages(images.toImageEntityList())
            downloadImagesAsync(images)

            Timber.d("Cached ${images.size} images for $normalized")
            ApiResult.Success(images)

        } catch (e: Exception) {
            Timber.e(e, "Error loading images for $breed")
            val cached = imageDao.getImagesByBreed(breed)
            if (cached.isNotEmpty()) {
                return@withContext ApiResult.Success(
                    cached.take(count ?: cached.size).toDomainImageList()
                )
            }
            ApiResult.Error(
                when (e) {
                    is DogsException -> e
                    else -> DogsException.UnknownException("Failed to get breed images", e)
                }
            )
        }
    }

    private fun mapImages(response: ImagesResponse, breed: String): List<DogImage> {
        return response.message.map { imageUrl ->
            DogImage(
                url = imageUrl,
                localPath = "",
                breed = breed
            )
        }
    }

    private fun downloadImagesAsync(images: List<DogImage>) {
        CoroutineScope(Dispatchers.IO).launch {
            images.forEach { image ->
                val localPath = downloadImage(image.url, image.breed)
                if (localPath != null) {
                    val updated = image.copy(localPath = localPath)
                    imageDao.updateImagePath(updated.url, updated.localPath, System.currentTimeMillis())
                    Timber.d("Downloaded image for ${image.breed} to $localPath")
                }
            }
        }
    }

    private fun downloadImage(url: String, breed: String): String? {
        return try {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val inputStream = response.body?.byteStream() ?: return null
                val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null

                val fileName = "${breed}_${System.currentTimeMillis()}.webp"
                val file = File(context.filesDir, fileName)

                FileOutputStream(file).use { out ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, out)
                    } else {
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 90, out)
                    }
                }

                file.absolutePath
            }
        } catch (e: Exception) {
            Timber.e(e, "Error downloading or converting image")
            null
        }
    }


    private fun mapException(exception: Exception): DogsException {
        return when (exception) {
            is HttpException -> {
                Timber.w("HTTP Exception: ${exception.code()} - ${exception.message()}")
                when (exception.code()) {
                    404 -> DogsException.InvalidBreedException("Breed not found")
                    in 400..499 -> DogsException.ApiException(
                        code = exception.code(),
                        message = "Client error: ${exception.message()}",
                        cause = exception
                    )

                    in 500..599 -> DogsException.ApiException(
                        code = exception.code(),
                        message = "Server error: ${exception.message()}",
                        cause = exception
                    )

                    else -> DogsException.ApiException(
                        code = exception.code(),
                        message = exception.message() ?: "HTTP error",
                        cause = exception
                    )
                }
            }

            is UnknownHostException -> {
                Timber.w("Network connection issue: ${exception.message}")
                DogsException.NetworkException(
                    message = "No internet connection",
                    cause = exception
                )
            }

            is SocketTimeoutException -> {
                Timber.w("Connection timeout: ${exception.message}")
                DogsException.NetworkException(
                    message = "Connection timeout",
                    cause = exception
                )
            }

            is IOException -> {
                Timber.w("Network IO error: ${exception.message}")
                DogsException.NetworkException(
                    message = "Network error: ${exception.message}",
                    cause = exception
                )
            }

            else -> {
                Timber.e(exception, "Unexpected error in repository")
                DogsException.UnknownException(
                    message = "Unexpected error: ${exception.message}",
                    cause = exception
                )
            }
        }
    }
}
