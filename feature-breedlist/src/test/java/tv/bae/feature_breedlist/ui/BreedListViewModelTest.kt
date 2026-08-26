package tv.bae.feature_breedlist.ui

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tv.bae.core.data.local.dao.FavouriteDao
import tv.bae.core.domain.repository.BreedRepository
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class BreedListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var breedRepository: BreedRepository
    private lateinit var favouriteDao: FavouriteDao
    private lateinit var toggleFavouriteUseCase: ToggleFavouriteUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        breedRepository = mockk()
        favouriteDao = mockk()
        toggleFavouriteUseCase = mockk()

        every { breedRepository.getBreedsPager() } returns flowOf(mockk(relaxed = true))
        every { favouriteDao.getAllFavouriteIdsFlow() } returns flowOf(emptyList())
        coEvery { toggleFavouriteUseCase(any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `favouriteIds starts empty then emits ids`() = runTest {
        every { favouriteDao.getAllFavouriteIdsFlow() } returns flowOf(
            emptyList(),
            listOf("stcat", "hcat"),
        )

        val viewModel = BreedListViewModel(breedRepository, favouriteDao, toggleFavouriteUseCase)

        viewModel.favouriteIds.test {
            assertTrue(awaitItem().isEmpty())
            val ids = awaitItem()
            assertTrue(ids.contains("stcat") && ids.contains("hcat"))
        }
    }

    @Test
    fun `toggleFavourite calls use case`() = runTest {
        val viewModel = BreedListViewModel(breedRepository, favouriteDao, toggleFavouriteUseCase)

        viewModel.toggleFavourite("stcat")
        advanceUntilIdle()

        coEvery { toggleFavouriteUseCase("stcat") }
    }
}
