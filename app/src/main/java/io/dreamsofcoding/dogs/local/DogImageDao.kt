package io.dreamsofcoding.dogs.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DogImageDao {

    @Query("SELECT * FROM dog_images WHERE breed = :breed ORDER BY cachedAt DESC")
    suspend fun getImagesByBreed(breed: String): List<DogImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<DogImageEntity>)

    @Query("DELETE FROM dog_images WHERE breed = :breed")
    suspend fun clearImagesForBreed(breed: String)

    @Query("UPDATE dog_images SET localPath = :path, cachedAt = :timestamp WHERE url = :url")
    suspend fun updateImagePath(url: String, path: String, timestamp: Long)
}