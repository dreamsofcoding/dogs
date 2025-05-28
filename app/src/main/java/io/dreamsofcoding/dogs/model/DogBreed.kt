package io.dreamsofcoding.dogs.model

import java.util.Collections

data class DogBreed(
    val name: String,
    val displayName: String = name.replaceFirstChar { it.uppercase() },
    val subBreeds: List<String> = Collections.emptyList()
)