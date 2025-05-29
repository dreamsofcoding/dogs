package io.dreamsofcoding.dogs.model

import kotlinx.serialization.Serializable

@Serializable
data class ListResponse(
    val message: Map<String, List<String>>,
    val status: String
)