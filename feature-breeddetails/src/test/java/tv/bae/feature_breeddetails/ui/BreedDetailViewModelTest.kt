package tv.bae.feature_breeddetails.ui

import app.cash.turbine.test
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
import tv.bae.core.domain.usecase.GetBreedDetailUseCase
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class BreedDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getBreedDetailUseCase: GetBreedDetailUseCase
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
        getBreedDetailUseCase = mockk()
        toggleFavouriteUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init emits Loading then Success`() = runTest {
        coEvery { getBreedDetailUseCase("stcat") } returns Result.success(fakeBreed)

        val viewModel = BreedDetailViewModel("stcat", getBreedDetailUseCase, toggleFavouriteUseCase)

        viewModel.uiState.test {
            assertEquals(BreedDetailUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is BreedDetailUiState.Success)
            assertEquals("stcat", (success as BreedDetailUiState.Success).breed.id)
            assertEquals("Street cat", success.breed.name)
        }
    }

    @Test
    fun `init emits Loading then Error`() = runTest {
        coEvery { getBreedDetailUseCase("stcat") } returns Result.failure(RuntimeException("Not found"))

        val viewModel = BreedDetailViewModel("stcat", getBreedDetailUseCase, toggleFavouriteUseCase)

        viewModel.uiState.test {
            assertEquals(BreedDetailUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is BreedDetailUiState.Error)
            assertEquals("Not found", (error as BreedDetailUiState.Error).message)
        }
    }

    @Test
    fun `toggleFavourite reloads breed detail`() = runTest {
        coEvery { getBreedDetailUseCase("stcat") } returns Result.success(fakeBreed)
        coEvery { toggleFavouriteUseCase("stcat") } returns Result.success(Unit)

        val viewModel = BreedDetailViewModel("stcat", getBreedDetailUseCase, toggleFavouriteUseCase)

        viewModel.uiState.test {
            assertEquals(BreedDetailUiState.Loading, awaitItem())
            awaitItem() // Success

            coEvery { getBreedDetailUseCase("stcat") } returns Result.success(
                fakeBreed.copy(isFavourite = true)
            )

            viewModel.toggleFavourite()
            assertEquals(BreedDetailUiState.Loading, awaitItem())
            val success = awaitItem() as BreedDetailUiState.Success
            assertTrue(success.breed.isFavourite)
        }
    }

    @Test
    fun `loadBreed reloads after error`() = runTest {
        coEvery { getBreedDetailUseCase("stcat") } returns Result.failure(RuntimeException("Error"))

        val viewModel = BreedDetailViewModel("stcat", getBreedDetailUseCase, toggleFavouriteUseCase)

        viewModel.uiState.test {
            assertEquals(BreedDetailUiState.Loading, awaitItem())
            assertTrue(awaitItem() is BreedDetailUiState.Error)

            coEvery { getBreedDetailUseCase("stcat") } returns Result.success(fakeBreed)

            viewModel.loadBreed()
            assertEquals(BreedDetailUiState.Loading, awaitItem())
            assertTrue(awaitItem() is BreedDetailUiState.Success)
        }
    }
}
