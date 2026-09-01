package tv.bae.core.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.repository.BreedRepository

class GetAverageLifespanUseCaseTest {

    private lateinit var repository: BreedRepository
    private lateinit var useCase: GetAverageLifespanUseCase

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
        repository = mockk()
        useCase = GetAverageLifespanUseCase(repository)
    }

    @Test
    fun `empty favourites returns null`() = runTest {
        coEvery { repository.getFavouriteBreeds() } returns Result.success(emptyList())
        val result = useCase()
        assertEquals(null, result.getOrNull())
    }

    @Test
    fun `single breed 12 - 15 returns 15 average`() = runTest {
        coEvery { repository.getFavouriteBreeds() } returns Result.success(listOf(fakeBreed.copy(lifeSpan = "12 - 15")))
        val result = useCase()
        assertEquals(15.0, result.getOrNull()!!, 0.001)
    }

    @Test
    fun `two breeds average is correct`() = runTest {
        coEvery { repository.getFavouriteBreeds() } returns Result.success(
            listOf(fakeBreed.copy(id = "a", lifeSpan = "12 - 15"), fakeBreed.copy(id = "b", lifeSpan = "10 - 14"))
        )
        val result = useCase() // (15+14)/2 = 14.5
        assertEquals(14.5, result.getOrNull()!!, 0.001)
    }

    @Test
    fun `malformed lifespan is ignored`() = runTest {
        coEvery { repository.getFavouriteBreeds() } returns Result.success(
            listOf(fakeBreed.copy(id = "a", lifeSpan = "unknown"), fakeBreed.copy(id = "b", lifeSpan = "12 - 15"))
        )
        val result = useCase()
        assertEquals(15.0, result.getOrNull()!!, 0.001)
    }

    @Test
    fun `empty lifespan is ignored returns null if all invalid`() = runTest {
        coEvery { repository.getFavouriteBreeds() } returns Result.success(
            listOf(fakeBreed.copy(id = "a", lifeSpan = ""), fakeBreed.copy(id = "b", lifeSpan = " - "))
        )
        val result = useCase()
        assertNull(result.getOrNull())
    }

    @Test
    fun `single value without dash returns that value`() = runTest {
        coEvery { repository.getFavouriteBreeds() } returns Result.success(listOf(fakeBreed.copy(lifeSpan = "12")))
        val result = useCase()
        assertEquals(12.0, result.getOrNull()!!, 0.001)
    }

    @Test
    fun `repository failure is propagated`() = runTest {
        coEvery { repository.getFavouriteBreeds() } returns Result.failure(RuntimeException("DB error"))
        val result = useCase()
        assertEquals(true, result.isFailure)
    }
}
