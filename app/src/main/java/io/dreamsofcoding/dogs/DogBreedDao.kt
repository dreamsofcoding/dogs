package io.dreamsofcoding.dogs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DogBreedDao {

    @Query("SELECT * FROM dog_breeds ORDER BY name ASC")
    suspend fun getAllBreeds(): List<DogBreedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreeds(breeds: List<DogBreedEntity>)

    @Query("DELETE FROM dog_breeds")
    suspend fun clearAll()
}