package io.dreamsofcoding.dogs

import kotlinx.serialization.Serializable

@Serializable
data class ListResponse(
    val message: Map<String, List<String>>,
    val status: String
)