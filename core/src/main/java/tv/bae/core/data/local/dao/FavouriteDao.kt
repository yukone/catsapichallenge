package tv.bae.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavouriteDao {
    @Query("SELECT breedId FROM favourites")
    suspend fun getAllFavouriteIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(breedId: String)

    @Query("DELETE FROM favourites WHERE breedId = :breedId")
    suspend fun delete(breedId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE breedId = :breedId)")
    suspend fun isFavourite(breedId: String): Boolean
}