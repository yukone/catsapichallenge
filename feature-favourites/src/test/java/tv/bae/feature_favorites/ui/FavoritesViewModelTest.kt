package tv.bae.feature_favorites.ui

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.usecase.GetAverageLifespanUseCase
import tv.bae.core.domain.usecase.GetFavouriteBreedsUseCase
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getFavouriteBreedsUseCase: GetFavouriteBreedsUseCase
    private lateinit var getAverageLifespanUseCase: GetAverageLifespanUseCase
    private lateinit var toggleFavouriteUseCase: ToggleFavouriteUseCase

    private val fakeBreed = Breed(
        id = "stcat",
        name = "Street cat",
        origin = "Valbom",
        temperament = "Active, energetic, noisy",
        description = "The Street cat aren't too friendly but they love food.",
        lifeSpan = "8 - 15",
        imageUrl = "https://example.com/stcat.jpg",
        isFavourite = true,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getFavouriteBreedsUseCase = mockk()
        getAverageLifespanUseCase = mockk()
        toggleFavouriteUseCase = mockk()
        coEvery { getAverageLifespanUseCase() } returns Result.success(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init emits Loading then Success`() = runTest {
        coEvery { getFavouriteBreedsUseCase() } returns Result.success(listOf(fakeBreed))
        coEvery { getAverageLifespanUseCase() } returns Result.success(15.0)

        val viewModel = FavoritesViewModel(
            getFavouriteBreedsUseCase,
            getAverageLifespanUseCase,
            toggleFavouriteUseCase,
        )

        viewModel.uiState.test {
            assertEquals(FavoritesUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is FavoritesUiState.Success)
            val data = success as FavoritesUiState.Success
            assertEquals(1, data.breeds.size)
            assertEquals(15.0, data.averageLifespan!!, 0.001)
        }
    }

    @Test
    fun `init emits Loading then Empty`() = runTest {
        coEvery { getFavouriteBreedsUseCase() } returns Result.success(emptyList())

        val viewModel = FavoritesViewModel(
            getFavouriteBreedsUseCase,
            getAverageLifespanUseCase,
            toggleFavouriteUseCase,
        )

        viewModel.uiState.test {
            assertEquals(FavoritesUiState.Loading, awaitItem())
            assertTrue(awaitItem() is FavoritesUiState.Empty)
        }
    }

    @Test
    fun `init emits Loading then Error`() = runTest {
        coEvery { getFavouriteBreedsUseCase() } returns Result.failure(RuntimeException("DB error"))

        val viewModel = FavoritesViewModel(
            getFavouriteBreedsUseCase,
            getAverageLifespanUseCase,
            toggleFavouriteUseCase,
        )

        viewModel.uiState.test {
            assertEquals(FavoritesUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is FavoritesUiState.Error)
            assertEquals("DB error", (error as FavoritesUiState.Error).message)
        }
    }

    @Test
    fun `removeFavourite reloads list`() = runTest {
        coEvery { getFavouriteBreedsUseCase() } returns Result.success(listOf(fakeBreed))
        coEvery { getAverageLifespanUseCase() } returns Result.success(15.0)
        coEvery { toggleFavouriteUseCase("stcat") } returns Result.success(Unit)

        val viewModel = FavoritesViewModel(
            getFavouriteBreedsUseCase,
            getAverageLifespanUseCase,
            toggleFavouriteUseCase,
        )

        viewModel.uiState.test {
            assertEquals(FavoritesUiState.Loading, awaitItem())
            awaitItem() // Success

            coEvery { getFavouriteBreedsUseCase() } returns Result.success(emptyList())

            viewModel.removeFavourite("stcat")
            assertEquals(FavoritesUiState.Loading, awaitItem())
            assertTrue(awaitItem() is FavoritesUiState.Empty)
        }
    }
}
