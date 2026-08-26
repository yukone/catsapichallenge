package tv.bae.core.data.repository

import tv.bae.core.data.local.dao.FavouriteDao
import tv.bae.core.data.local.entities.FavouriteEntity
import tv.bae.core.data.remote.CatApi
import tv.bae.core.data.remote.mapper.toDomain
import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.repository.BreedRepository

class BreedRepositoryImpl(
    private val catApi: CatApi,
    private val favouriteDao: FavouriteDao,
) : BreedRepository {

    override suspend fun getBreeds(page: Int): Result<List<Breed>> {
        return try {
            val dtos = catApi.getBreeds(page = page)
            val favIds = favouriteDao.getAllFavouriteIds().toSet()
            val breeds = dtos.map { it.toDomain().copy(isFavourite = it.id in favIds) }
            Result.success(breeds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBreedById(id: String): Result<Breed> {
        return try {
            val breeds = catApi.getBreeds()
            val breed = breeds.first { it.id == id }
            val isFav = favouriteDao.isFavourite(id)
            Result.success(breed.toDomain().copy(isFavourite = isFav))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFavouriteBreeds(): Result<List<Breed>> {
        return try {
            val favIds = favouriteDao.getAllFavouriteIds()
            val allBreeds = catApi.getBreeds(page = 0, limit = 100)
            val breedMap = allBreeds.associateBy { it.id }
            val breeds = favIds.mapNotNull { id ->
                breedMap[id]?.toDomain()?.copy(isFavourite = true)
            }
            Result.success(breeds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavourite(breedId: String): Result<Unit> {
        return try {
            if (favouriteDao.isFavourite(breedId)) {
                favouriteDao.delete(breedId)
            } else {
                favouriteDao.insert(FavouriteEntity(breedId))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
