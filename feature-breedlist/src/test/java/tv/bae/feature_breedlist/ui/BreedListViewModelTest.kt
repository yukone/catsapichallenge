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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tv.bae.core.data.local.dao.FavouriteDao
import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.repository.BreedRepository
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class BreedListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var breedRepository: BreedRepository
    private lateinit var favouriteDao: FavouriteDao
    private lateinit var toggleFavouriteUseCase: ToggleFavouriteUseCase

    private val fakeBreed = Breed(
        id = "stcat",
        name = "Street cat",
        origin = "Valbom",
        temperament = "Active, energetic, noisy",
        description = "The Street cat aren't too friendly but they love food.",
        lifeSpan = "8 - 15",
        imageUrl = "https://example.com/stcat.jpg",
    )

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

    @Test
    fun `searchQuery starts empty`() = runTest {
        val viewModel = BreedListViewModel(breedRepository, favouriteDao, toggleFavouriteUseCase)

        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `onSearchQueryChange updates query`() = runTest {
        val viewModel = BreedListViewModel(breedRepository, favouriteDao, toggleFavouriteUseCase)

        viewModel.onSearchQueryChange("street")
        assertEquals("street", viewModel.searchQuery.value)
    }

    @Test
    fun `searchResults emits filtered breeds`() = runTest {
        coEvery { breedRepository.searchBreeds("street") } returns Result.success(listOf(fakeBreed))

        val viewModel = BreedListViewModel(breedRepository, favouriteDao, toggleFavouriteUseCase)

        viewModel.searchResults.test {
            assertEquals(emptyList<Breed>(), awaitItem())

            viewModel.onSearchQueryChange("street")
            assertEquals(listOf(fakeBreed), awaitItem())
        }
    }

    @Test
    fun `clearSearch resets query`() = runTest {
        val viewModel = BreedListViewModel(breedRepository, favouriteDao, toggleFavouriteUseCase)

        viewModel.onSearchQueryChange("street")
        assertEquals("street", viewModel.searchQuery.value)

        viewModel.clearSearch()
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `empty search query emits empty results`() = runTest {
        val viewModel = BreedListViewModel(breedRepository, favouriteDao, toggleFavouriteUseCase)

        viewModel.searchResults.test {
            assertEquals(emptyList<Breed>(), awaitItem())
        }
    }
}
