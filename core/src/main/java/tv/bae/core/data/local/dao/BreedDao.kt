package tv.bae.core.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tv.bae.core.data.local.entities.BreedEntity

@Dao
interface BreedDao {
    @Query("SELECT * FROM breeds ORDER BY page ASC, name ASC")
    fun pagingSource(): PagingSource<Int, BreedEntity>

    @Query("SELECT * FROM breeds WHERE id = :id")
    suspend fun getById(id: String): BreedEntity?

    @Query("SELECT * FROM breeds WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<BreedEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breeds: List<BreedEntity>)

    @Query("DELETE FROM breeds")
    suspend fun clear()

    @Query("SELECT MIN(cachedAt) FROM breeds")
    suspend fun getOldestCacheTime(): Long?

    @Query("SELECT COUNT(*) FROM breeds")
    suspend fun count(): Int

    @Query("SELECT * FROM breeds WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchByName(query: String): List<BreedEntity>
}
