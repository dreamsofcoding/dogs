package io.dreamsofcoding.dogs

import kotlinx.serialization.Serializable

@Serializable
data class ImagesResponse(
    val message: List<String>,
    val status: String
)