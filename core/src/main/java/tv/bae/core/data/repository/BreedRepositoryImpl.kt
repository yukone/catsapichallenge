package tv.bae.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import tv.bae.core.data.local.dao.BreedDao
import tv.bae.core.data.local.dao.FavouriteDao
import tv.bae.core.data.local.entities.BreedEntity
import tv.bae.core.data.local.entities.FavouriteEntity
import tv.bae.core.data.local.mapper.toCacheEntity
import tv.bae.core.data.local.mapper.toDomain
import tv.bae.core.data.remote.BreedRemoteMediator
import tv.bae.core.data.remote.CatApi
import tv.bae.core.data.remote.mapper.toDomain
import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.repository.BreedRepository

@OptIn(ExperimentalPagingApi::class)
class BreedRepositoryImpl(
    private val catApi: CatApi,
    private val favouriteDao: FavouriteDao,
    private val breedDao: BreedDao,
) : BreedRepository {

    override fun getBreedsPager(): Flow<PagingData<BreedEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            remoteMediator = BreedRemoteMediator(catApi, breedDao),
            pagingSourceFactory = { breedDao.pagingSource() },
        ).flow
    }

    override suspend fun getBreedById(id: String): Result<Breed> {
        return try {
            val cached = breedDao.getById(id)
            if (cached != null) {
                val isFav = favouriteDao.isFavourite(id)
                return Result.success(cached.toDomain().copy(isFavourite = isFav))
            }
            val breeds = catApi.getBreeds(page = 0, limit = 100)
            val breed = breeds.firstOrNull { it.id == id }
                ?: return Result.failure(NoSuchElementException("Breed not found"))
            val isFav = favouriteDao.isFavourite(id)
            breedDao.insertAll(listOf(breed.toCacheEntity(0)))
            Result.success(breed.toDomain().copy(isFavourite = isFav))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFavouriteBreeds(): Result<List<Breed>> {
        return try {
            val favIds = favouriteDao.getAllFavouriteIds()
            if (favIds.isEmpty()) return Result.success(emptyList())
            val cachedBreeds = breedDao.getByIds(favIds)
            if (cachedBreeds.isNotEmpty()) {
                return Result.success(cachedBreeds.map { it.toDomain().copy(isFavourite = true) })
            }
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
