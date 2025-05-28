package io.dreamsofcoding.dogs

import retrofit2.http.GET
import retrofit2.http.Path

interface DogService {

    /**
     * Get all available dog breeds
     * API: https://dog.ceo/api/breeds/list/all
     */
    @GET("breeds/list/all")
    suspend fun getBreeds(): ListResponse

    /**
     * Get all images for a specific breed
     * API: https://dog.ceo/api/breed/{breed}/images
     */
    @GET("breed/{breed}/images")
    suspend fun getAllBreedImages(@Path("breed") breed: String): ImagesResponse

    /**
     * Get random images for a specific breed
     * API: https://dog.ceo/api/breed/{breed}/images/random/{count}
     */
    @GET("breed/{breed}/images/random/{count}")
    suspend fun getBreedImages(
        @Path("breed") breed: String,
        @Path("count") count: Int? = 10
    ): ImagesResponse

    companion object {
        const val BASE_URL = "https://dog.ceo/api/"
    }
}