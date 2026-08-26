package tv.bae.core.data.local

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tv.bae.core.data.local.dao.BreedDao
import tv.bae.core.data.local.entities.BreedEntity

@RunWith(AndroidJUnit4::class)
class BreedDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: BreedDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.breedDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun fakeBreed(id: String, name: String = "Breed $id", page: Int = 0, cachedAt: Long = System.currentTimeMillis()) = BreedEntity(
        id = id,
        name = name,
        origin = "Origin",
        temperament = "Calm",
        description = "A breed",
        lifeSpan = "10 - 15",
        imageUrl = null,
        page = page,
        cachedAt = cachedAt,
    )

    @Test
    fun insertAll_and_count() = runTest {
        dao.insertAll(listOf(fakeBreed("a"), fakeBreed("b"), fakeBreed("c")))

        assertEquals(3, dao.count())
    }

    @Test
    fun insertAll_replaces_on_conflict() = runTest {
        dao.insertAll(listOf(fakeBreed("a", name = "Original")))
        dao.insertAll(listOf(fakeBreed("a", name = "Updated")))

        val breed = dao.getById("a")
        assertNotNull(breed)
        assertEquals("Updated", breed!!.name)
        assertEquals(1, dao.count())
    }

    @Test
    fun getById_returns_breed() = runTest {
        dao.insertAll(listOf(fakeBreed("stcat", name = "Street cat")))

        val breed = dao.getById("stcat")

        assertNotNull(breed)
        assertEquals("Street cat", breed!!.name)
    }

    @Test
    fun getById_returns_null_when_not_found() = runTest {
        assertNull(dao.getById("unknown"))
    }

    @Test
    fun getByIds_returns_matching_breeds() = runTest {
        dao.insertAll(listOf(fakeBreed("a"), fakeBreed("b"), fakeBreed("c")))

        val results = dao.getByIds(listOf("a", "c"))

        assertEquals(2, results.size)
        assertTrue(results.any { it.id == "a" })
        assertTrue(results.any { it.id == "c" })
    }

    @Test
    fun getByIds_returns_empty_for_no_matches() = runTest {
        dao.insertAll(listOf(fakeBreed("a")))

        val results = dao.getByIds(listOf("x", "y"))

        assertTrue(results.isEmpty())
    }

    @Test
    fun clear_removes_all_breeds() = runTest {
        dao.insertAll(listOf(fakeBreed("a"), fakeBreed("b")))

        dao.clear()

        assertEquals(0, dao.count())
    }

    @Test
    fun getOldestCacheTime_returns_min() = runTest {
        dao.insertAll(listOf(
            fakeBreed("a", cachedAt = 1000L),
            fakeBreed("b", cachedAt = 500L),
            fakeBreed("c", cachedAt = 2000L),
        ))

        val oldest = dao.getOldestCacheTime()

        assertEquals(500L, oldest)
    }

    @Test
    fun getOldestCacheTime_returns_null_when_empty() = runTest {
        assertNull(dao.getOldestCacheTime())
    }

    @Test
    fun searchByName_finds_by_name() = runTest {
        dao.insertAll(listOf(
            fakeBreed("a", name = "Street cat"),
            fakeBreed("b", name = "House cat"),
            fakeBreed("c", name = "Tiger"),
        ))

        val results = dao.searchByName("cat")

        assertEquals(2, results.size)
        assertTrue(results.all { it.name.contains("cat", ignoreCase = true) })
    }

    @Test
    fun searchByName_returns_empty_when_no_match() = runTest {
        dao.insertAll(listOf(fakeBreed("a", name = "Street cat")))

        val results = dao.searchByName("dragon")

        assertTrue(results.isEmpty())
    }

    @Test
    fun searchByName_orders_by_name() = runTest {
        dao.insertAll(listOf(
            fakeBreed("a", name = "Zebra cat"),
            fakeBreed("b", name = "Alpha cat"),
            fakeBreed("c", name = "Middle cat"),
        ))

        val results = dao.searchByName("cat")

        assertEquals(3, results.size)
        assertEquals("Alpha cat", results[0].name)
        assertEquals("Middle cat", results[1].name)
        assertEquals("Zebra cat", results[2].name)
    }

    @Test
    fun pagingSource_returns_breeds_ordered_by_page_and_name() = runTest {
        dao.insertAll(listOf(
            fakeBreed("b", name = "Bravo", page = 0),
            fakeBreed("a", name = "Alpha", page = 1),
            fakeBreed("c", name = "Charlie", page = 0),
        ))

        val pagingSource = dao.pagingSource()
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val data = (result as PagingSource.LoadResult.Page).data
        assertEquals(3, data.size)
        assertEquals("Bravo", data[0].name)
        assertEquals("Charlie", data[1].name)
        assertEquals("Alpha", data[2].name)
    }
}
