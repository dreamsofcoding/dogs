package io.dreamsofcoding.dogs.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        DogBreedEntity::class,
        DogImageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class DogDatabase : RoomDatabase() {

    abstract fun breedDao(): DogBreedDao
    abstract fun imageDao(): DogImageDao

    companion object {
        const val DATABASE_NAME = "dog_database"

        // Cache expiration times
        const val CACHE_DURATION_BREEDS = 24 * 60 * 60 * 1000L
        const val CACHE_DURATION_IMAGES = 7 * 24 * 60 * 60 * 1000L
    }
}