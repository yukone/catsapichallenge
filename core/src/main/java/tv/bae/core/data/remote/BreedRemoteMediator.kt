package tv.bae.core.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.APPEND
import androidx.paging.LoadType.PREPEND
import androidx.paging.LoadType.REFRESH
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import tv.bae.core.data.local.dao.BreedDao
import tv.bae.core.data.local.entities.BreedEntity
import tv.bae.core.data.local.mapper.toCacheEntity

private const val CACHE_TIMEOUT_MS = 60 * 60 * 1000L // 1 hour

@OptIn(ExperimentalPagingApi::class)
class BreedRemoteMediator(
    private val catApi: CatApi,
    private val breedDao: BreedDao,
) : RemoteMediator<Int, BreedEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, BreedEntity>,
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                REFRESH -> 0
                PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        0
                    } else {
                        lastItem.page + 1
                    }
                }
            }

            val dtos = catApi.getBreeds(page = page, limit = state.config.pageSize)
            if (loadType == REFRESH) {
                breedDao.clear()
            }
            breedDao.insertAll(dtos.map { it.toCacheEntity(page) })

            MediatorResult.Success(endOfPaginationReached = dtos.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val oldestCacheTime = breedDao.getOldestCacheTime() ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val isStale = System.currentTimeMillis() - oldestCacheTime > CACHE_TIMEOUT_MS
        return if (isStale) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }
}
