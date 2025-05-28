package io.dreamsofcoding.dogs.di

import android.content.Context
import androidx.room.Room
import coil.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.dreamsofcoding.dogs.DogBreedDao
import io.dreamsofcoding.dogs.DogDatabase
import io.dreamsofcoding.dogs.DogImageDao
import io.dreamsofcoding.dogs.DogService
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext ctx: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                Timber.d("Making API request to: ${request.url}")

                val startTime = System.currentTimeMillis()
                val response = chain.proceed(request)
                val endTime = System.currentTimeMillis()

                Timber.d("API request completed in ${endTime - startTime}ms - Status: ${response.code}")

                if (!response.isSuccessful) {
                    Timber.w("API request failed with status: ${response.code} - ${response.message}")
                }
                response
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cache(Cache(ctx.cacheDir, 10L * 1024 * 1024))
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DogService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDogApiService(retrofit: Retrofit): DogService {
        return retrofit.create(DogService::class.java)
    }


    @Provides
    @Singleton
    fun provideDogDatabase(
        @ApplicationContext context: Context
    ): DogDatabase {
        return Room.databaseBuilder(
            context,
            DogDatabase::class.java,
            DogDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
    }

    @Provides
    fun provideDogBreedDao(database: DogDatabase): DogBreedDao {
        return database.breedDao()
    }

    @Provides
    fun provideDogImageDao(database: DogDatabase): DogImageDao {
        return database.imageDao()
    }

}