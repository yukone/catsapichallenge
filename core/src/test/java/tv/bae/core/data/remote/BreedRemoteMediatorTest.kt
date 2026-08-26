package tv.bae.core.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.APPEND
import androidx.paging.LoadType.PREPEND
import androidx.paging.LoadType.REFRESH
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.RemoteMediator.InitializeAction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tv.bae.core.data.local.dao.BreedDao
import tv.bae.core.data.local.entities.BreedEntity
import tv.bae.core.data.remote.dto.BreedDto
import tv.bae.core.data.remote.dto.BreedImageDto

@OptIn(ExperimentalPagingApi::class)
class BreedRemoteMediatorTest {

    private lateinit var breedDao: BreedDao
    private lateinit var catApi: CatApi
    private lateinit var mediator: BreedRemoteMediator

    @Before
    fun setup() {
        breedDao = mockk(relaxed = true)
        catApi = mockk()
        mediator = BreedRemoteMediator(catApi, breedDao)
    }

    private fun fakeDto(id: String, name: String = "Breed $id") = BreedDto(
        id = id,
        name = name,
        origin = "Origin",
        temperament = "Calm",
        description = "A breed",
        lifeSpan = "10 - 15",
        image = BreedImageDto(url = "https://example.com/$id.jpg"),
    )

    private fun fakeEntity(id: String, page: Int = 0, cachedAt: Long = System.currentTimeMillis()) = BreedEntity(
        id = id,
        name = "Breed $id",
        origin = "Origin",
        temperament = "Calm",
        description = "A breed",
        lifeSpan = "10 - 15",
        imageUrl = null,
        page = page,
        cachedAt = cachedAt,
    )

    private fun fakeState(lastItem: BreedEntity? = null) = PagingState<Int, BreedEntity>(
        pages = if (lastItem != null) listOf(
            PagingSource.LoadResult.Page(data = listOf(lastItem), prevKey = null, nextKey = null)
        ) else listOf(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0,
    )

    @Test
    fun refresh_fetches_first_page() = runTest {
        coEvery { catApi.getBreeds(page = 0, limit = 20) } returns listOf(fakeDto("a"), fakeDto("b"))

        val result = mediator.load(REFRESH, fakeState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify { breedDao.clear() }
        coVerify { breedDao.insertAll(match { it.size == 2 }) }
    }

    @Test
    fun prepend_always_returns_end_of_pagination() = runTest {
        val result = mediator.load(PREPEND, fakeState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun append_loads_next_page_based_on_last_item() = runTest {
        val lastItem = fakeEntity("a", page = 2)
        coEvery { breedDao.getById("a") } returns lastItem
        coEvery { catApi.getBreeds(page = 3, limit = 20) } returns listOf(fakeDto("b"))

        val result = mediator.load(APPEND, fakeState(lastItem))

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        coVerify { catApi.getBreeds(page = 3, limit = 20) }
    }

    @Test
    fun append_uses_page_0_when_no_last_item() = runTest {
        coEvery { catApi.getBreeds(page = 0, limit = 20) } returns emptyList()

        val result = mediator.load(APPEND, fakeState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun append_returns_end_when_api_returns_empty() = runTest {
        val lastItem = fakeEntity("a", page = 0)
        coEvery { breedDao.getById("a") } returns lastItem
        coEvery { catApi.getBreeds(page = 1, limit = 20) } returns emptyList()

        val result = mediator.load(APPEND, fakeState(lastItem))

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun load_returns_error_on_exception() = runTest {
        coEvery { catApi.getBreeds(any(), any()) } throws RuntimeException("Network error")

        val result = mediator.load(REFRESH, fakeState())

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertEquals("Network error", (result as RemoteMediator.MediatorResult.Error).throwable.message)
    }

    @Test
    fun initialize_launches_refresh_when_cache_is_empty() = runTest {
        coEvery { breedDao.getOldestCacheTime() } returns null

        val action = mediator.initialize()

        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, action)
    }

    @Test
    fun initialize_skips_refresh_when_cache_is_fresh() = runTest {
        coEvery { breedDao.getOldestCacheTime() } returns System.currentTimeMillis()

        val action = mediator.initialize()

        assertEquals(InitializeAction.SKIP_INITIAL_REFRESH, action)
    }

    @Test
    fun initialize_launches_refresh_when_cache_is_stale() = runTest {
        val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000L)
        coEvery { breedDao.getOldestCacheTime() } returns twoHoursAgo

        val action = mediator.initialize()

        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, action)
    }
}
