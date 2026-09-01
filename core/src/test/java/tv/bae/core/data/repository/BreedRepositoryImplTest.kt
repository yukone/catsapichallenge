package tv.bae.core.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tv.bae.core.data.local.AppDatabase
import tv.bae.core.data.local.dao.BreedDao
import tv.bae.core.data.local.dao.FavouriteDao
import tv.bae.core.data.local.entities.FavouriteEntity
import tv.bae.core.data.remote.CatApi
import tv.bae.core.data.remote.dto.BreedDto
import tv.bae.core.data.remote.dto.BreedImageDto

class BreedRepositoryImplTest {

    private lateinit var catApi: CatApi
    private lateinit var favouriteDao: FavouriteDao
    private lateinit var breedDao: BreedDao
    private lateinit var database: AppDatabase
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
        breedDao = mockk()
        database = mockk(relaxed = true)
        repository = BreedRepositoryImpl(catApi, favouriteDao, breedDao, database)
    }

    @Test
    fun `getBreedById returns breed from cache when available`() = runTest {
        val cachedEntity = tv.bae.core.data.local.entities.BreedEntity(
            id = "stcat",
            name = "Street cat",
            origin = "Valbom",
            temperament = "Active, energetic, noisy",
            description = "The Street cat aren't too friendly but they love food.",
            lifeSpan = "8 - 15",
            imageUrl = "https://example.com/stcat.jpg",
            page = 0,
            cachedAt = System.currentTimeMillis(),
        )
        coEvery { breedDao.getById("stcat") } returns cachedEntity
        coEvery { favouriteDao.isFavourite("stcat") } returns true

        val result = repository.getBreedById("stcat")

        assertTrue(result.isSuccess)
        val breed = result.getOrNull()!!
        assertEquals("stcat", breed.id)
        assertEquals("Street cat", breed.name)
        assertTrue(breed.isFavourite)
    }

    @Test
    fun `getBreedById fetches from API when not cached`() = runTest {
        coEvery { breedDao.getById("stcat") } returns null
        coEvery { catApi.getBreeds(page = 0, limit = 100) } returns listOf(fakeDto, fakeDto2)
        coEvery { favouriteDao.isFavourite("stcat") } returns false
        coEvery { breedDao.insertAll(any()) } returns Unit

        val result = repository.getBreedById("stcat")

        assertTrue(result.isSuccess)
        val breed = result.getOrNull()!!
        assertEquals("stcat", breed.id)
    }

    @Test
    fun `getBreedById returns failure when breed not found`() = runTest {
        coEvery { breedDao.getById("unknown") } returns null
        coEvery { catApi.getBreeds(page = 0, limit = 100) } returns listOf(fakeDto)

        val result = repository.getBreedById("unknown")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `getFavouriteBreeds returns from cache when available`() = runTest {
        coEvery { favouriteDao.getAllFavouriteIds() } returns listOf("stcat", "unknown")
        val cachedEntities = listOf(
            tv.bae.core.data.local.entities.BreedEntity(
                id = "stcat",
                name = "Street cat",
                origin = "Valbom",
                temperament = "Active",
                description = "Test",
                lifeSpan = "8 - 15",
                imageUrl = null,
                page = 0,
                cachedAt = System.currentTimeMillis(),
            ),
        )
        coEvery { breedDao.getByIds(listOf("stcat", "unknown")) } returns cachedEntities

        val result = repository.getFavouriteBreeds()

        assertTrue(result.isSuccess)
        val breeds = result.getOrNull()!!
        assertEquals(1, breeds.size)
        assertEquals("stcat", breeds[0].id)
        assertTrue(breeds[0].isFavourite)
    }

    @Test
    fun `getFavouriteBreeds fetches from API when cache empty`() = runTest {
        coEvery { favouriteDao.getAllFavouriteIds() } returns listOf("stcat", "unknown")
        coEvery { breedDao.getByIds(listOf("stcat", "unknown")) } returns emptyList()
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
