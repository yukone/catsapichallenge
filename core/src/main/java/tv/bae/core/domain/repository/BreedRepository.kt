package tv.bae.core.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import tv.bae.core.data.local.entities.BreedEntity
import tv.bae.core.domain.model.Breed

interface BreedRepository {
    fun getBreedsPager(): Flow<PagingData<BreedEntity>>
    suspend fun getBreedById(id: String): Result<Breed>
    suspend fun getFavouriteBreeds(): Result<List<Breed>>
    suspend fun toggleFavourite(breedId: String): Result<Unit>
}
