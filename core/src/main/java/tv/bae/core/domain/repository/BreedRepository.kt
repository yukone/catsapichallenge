package tv.bae.core.domain.repository

import tv.bae.core.domain.model.Breed

interface BreedRepository {
    suspend fun getBreeds(page: Int): Result<List<Breed>>
    suspend fun getBreedById(id: String): Result<Breed>
    suspend fun getFavouriteBreeds(): Result<List<Breed>>
    suspend fun toggleFavourite(breedId: String): Result<Unit>
}
