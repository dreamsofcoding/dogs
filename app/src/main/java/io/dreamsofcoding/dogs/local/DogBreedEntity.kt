package io.dreamsofcoding.dogs.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "dog_breeds")
@TypeConverters(StringListConverter::class)
data class DogBreedEntity(
    @PrimaryKey
    val name: String,
    val subBreeds: List<String>,
    val cachedAt: Long = System.currentTimeMillis()
)