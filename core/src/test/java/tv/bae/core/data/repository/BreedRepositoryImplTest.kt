package tv.bae.core.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tv.bae.core.data.local.dao.FavouriteDao
import tv.bae.core.data.local.entities.FavouriteEntity
import tv.bae.core.data.remote.CatApi
import tv.bae.core.data.remote.dto.BreedDto
import tv.bae.core.data.remote.dto.BreedImageDto

class BreedRepositoryImplTest {

    private lateinit var catApi: CatApi
    private lateinit var favouriteDao: FavouriteDao
    private lateinit var repository: BreedRepositoryImpl

    private val fakeDto = BreedDto(
        id = "stcat",
        name = "Street cat",
        origin = "Valbom",
        temperament = "Active, energetic, noisy",
        description = "The Street cat aren't too friendly but they love food.",
        lifeSpan = "8 - 15",
        image = BreedImageDto(url = "https://example.com/stcat.jpg"),
    )

    private val fakeDto2 = BreedDto(
        id = "hcat",
        name = "Home cat",
        origin = "Valbom",
        temperament = "Passive, lazy, quiet",
        description = "The Home cat love to sit on windows and watch the street.",
        lifeSpan = "8 - 15",
        image = null,
    )

    @Before
    fun setup() {
        catApi = mockk()
        favouriteDao = mockk()
        repository = BreedRepositoryImpl(catApi, favouriteDao)
    }

    @Test
    fun `getBreeds returns mapped breeds with favourite status`() = runTest {
        coEvery { catApi.getBreeds(page = 0) } returns listOf(fakeDto, fakeDto2)
        coEvery { favouriteDao.getAllFavouriteIds() } returns listOf("stcat")

        val result = repository.getBreeds(page = 0)

        assertTrue(result.isSuccess)
        val breeds = result.getOrNull()!!
        assertEquals(2, breeds.size)
        assertEquals("stcat", breeds[0].id)
        assertEquals("Street cat", breeds[0].name)
        assertTrue(breeds[0].isFavourite)
        assertEquals("hcat", breeds[1].id)
        assertEquals(false, breeds[1].isFavourite)
    }

    @Test
    fun `getBreeds returns failure on exception`() = runTest {
        coEvery { catApi.getBreeds(any()) } throws RuntimeException("Network error")

        val result = repository.getBreeds(page = 0)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getBreedById returns breed when found`() = runTest {
        coEvery { catApi.getBreeds(page = 0, limit = 100) } returns listOf(fakeDto, fakeDto2)
        coEvery { favouriteDao.isFavourite("stcat") } returns true

        val result = repository.getBreedById("stcat")

        assertTrue(result.isSuccess)
        val breed = result.getOrNull()!!
        assertEquals("stcat", breed.id)
        assertEquals("Street cat", breed.name)
        assertTrue(breed.isFavourite)
    }

    @Test
    fun `getBreedById returns failure when breed not found`() = runTest {
        coEvery { catApi.getBreeds(page = 0, limit = 100) } returns listOf(fakeDto)

        val result = repository.getBreedById("unknown")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `getFavouriteBreeds returns only favourited breeds`() = runTest {
        coEvery { favouriteDao.getAllFavouriteIds() } returns listOf("stcat", "unknown")
        coEvery { catApi.getBreeds(page = 0, limit = 100) } returns listOf(fakeDto, fakeDto2)

        val result = repository.getFavouriteBreeds()

        assertTrue(result.isSuccess)
        val breeds = result.getOrNull()!!
        assertEquals(1, breeds.size)
        assertEquals("stcat", breeds[0].id)
        assertTrue(breeds[0].isFavourite)
    }

    @Test
    fun `getFavouriteBreeds returns empty when no favourites`() = runTest {
        coEvery { favouriteDao.getAllFavouriteIds() } returns emptyList()
        coEvery { catApi.getBreeds(page = 0, limit = 100) } returns listOf(fakeDto)

        val result = repository.getFavouriteBreeds()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `toggleFavourite inserts when not favourite`() = runTest {
        coEvery { favouriteDao.isFavourite("stcat") } returns false
        coEvery { favouriteDao.insert(any()) } returns Unit

        val result = repository.toggleFavourite("stcat")

        assertTrue(result.isSuccess)
        coVerify { favouriteDao.insert(FavouriteEntity("stcat")) }
    }

    @Test
    fun `toggleFavourite deletes when already favourite`() = runTest {
        coEvery { favouriteDao.isFavourite("stcat") } returns true
        coEvery { favouriteDao.delete("stcat") } returns Unit

        val result = repository.toggleFavourite("stcat")

        assertTrue(result.isSuccess)
        coVerify { favouriteDao.delete("stcat") }
    }
}
