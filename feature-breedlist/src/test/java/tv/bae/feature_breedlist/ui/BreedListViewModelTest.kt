package tv.bae.feature_breedlist.ui

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.usecase.GetBreedsUseCase
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class BreedListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getBreedsUseCase: GetBreedsUseCase
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

    private val fakeBreed2 = Breed(
        id = "hcat",
        name = "Home cat",
        origin = "Valbom",
        temperament = "Passive, lazy, quiet",
        description = "The Home cat love to sit on windows and watch the street.",
        lifeSpan = "8 - 15",
        imageUrl = null,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getBreedsUseCase = mockk()
        toggleFavouriteUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads breeds and emits Success`() = runTest {
        coEvery { getBreedsUseCase(page = 0) } returns Result.success(listOf(fakeBreed, fakeBreed2))

        val viewModel = BreedListViewModel(getBreedsUseCase, toggleFavouriteUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BreedListUiState.Success)
        assertEquals(2, (state as BreedListUiState.Success).breeds.size)
        assertEquals("stcat", state.breeds[0].id)
    }

    @Test
    fun `init with empty result emits Empty`() = runTest {
        coEvery { getBreedsUseCase(page = 0) } returns Result.success(emptyList())

        val viewModel = BreedListViewModel(getBreedsUseCase, toggleFavouriteUseCase)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is BreedListUiState.Empty)
    }

    @Test
    fun `init with failure emits Error`() = runTest {
        coEvery { getBreedsUseCase(page = 0) } returns Result.failure(RuntimeException("Network error"))

        val viewModel = BreedListViewModel(getBreedsUseCase, toggleFavouriteUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BreedListUiState.Error)
        assertEquals("Network error", (state as BreedListUiState.Error).message)
    }

    @Test
    fun `loadNextPage appends breeds`() = runTest {
        coEvery { getBreedsUseCase(page = 0) } returns Result.success(listOf(fakeBreed))
        coEvery { getBreedsUseCase(page = 1) } returns Result.success(listOf(fakeBreed2))

        val viewModel = BreedListViewModel(getBreedsUseCase, toggleFavouriteUseCase)
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value as BreedListUiState.Success
        assertEquals(2, state.breeds.size)
        assertEquals("stcat", state.breeds[0].id)
        assertEquals("hcat", state.breeds[1].id)
    }

    @Test
    fun `loadNextPage does not load when reached end`() = runTest {
        coEvery { getBreedsUseCase(page = 0) } returns Result.success(emptyList())

        val viewModel = BreedListViewModel(getBreedsUseCase, toggleFavouriteUseCase)
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is BreedListUiState.Empty)
    }

    @Test
    fun `toggleFavourite refreshes list`() = runTest {
        coEvery { getBreedsUseCase(page = 0) } returns Result.success(listOf(fakeBreed))
        coEvery { toggleFavouriteUseCase("stcat") } returns Result.success(Unit)

        val viewModel = BreedListViewModel(getBreedsUseCase, toggleFavouriteUseCase)
        advanceUntilIdle()

        viewModel.toggleFavourite("stcat")
        advanceUntilIdle()

        coEvery { getBreedsUseCase(page = 0) } returns Result.success(
            listOf(fakeBreed.copy(isFavourite = true))
        )
        viewModel.toggleFavourite("stcat")
        advanceUntilIdle()

        val state = viewModel.uiState.value as BreedListUiState.Success
        assertTrue(state.breeds[0].isFavourite)
    }

    @Test
    fun `refreshIfNeeded updates favourite status`() = runTest {
        coEvery { getBreedsUseCase(page = 0) } returns Result.success(listOf(fakeBreed))

        val viewModel = BreedListViewModel(getBreedsUseCase, toggleFavouriteUseCase)
        advanceUntilIdle()

        coEvery { getBreedsUseCase(page = 0) } returns Result.success(
            listOf(fakeBreed.copy(isFavourite = true))
        )
        viewModel.refreshIfNeeded()
        advanceUntilIdle()

        val state = viewModel.uiState.value as BreedListUiState.Success
        assertTrue(state.breeds[0].isFavourite)
    }

    @Test
    fun `refreshIfNeeded does nothing when list is empty`() = runTest {
        coEvery { getBreedsUseCase(page = 0) } returns Result.success(emptyList())

        val viewModel = BreedListViewModel(getBreedsUseCase, toggleFavouriteUseCase)
        advanceUntilIdle()

        viewModel.refreshIfNeeded()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is BreedListUiState.Empty)
    }
}
