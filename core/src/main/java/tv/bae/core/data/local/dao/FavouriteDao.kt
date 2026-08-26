package tv.bae.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tv.bae.core.data.local.entities.FavouriteEntity

@Dao
interface FavouriteDao {
    @Query("SELECT breedId FROM favourites")
    suspend fun getAllFavouriteIds(): List<String>

    @Query("SELECT breedId FROM favourites")
    fun getAllFavouriteIdsFlow(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favourite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE breedId = :breedId")
    suspend fun delete(breedId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE breedId = :breedId)")
    suspend fun isFavourite(breedId: String): Boolean
}