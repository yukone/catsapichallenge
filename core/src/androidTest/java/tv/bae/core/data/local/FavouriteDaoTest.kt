package tv.bae.core.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tv.bae.core.data.local.dao.FavouriteDao
import tv.bae.core.data.local.entities.FavouriteEntity

@RunWith(AndroidJUnit4::class)
class FavouriteDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FavouriteDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.favouriteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insert_adds_favourite() = runTest {
        dao.insert(FavouriteEntity("stcat"))

        val ids = dao.getAllFavouriteIds()

        assertEquals(1, ids.size)
        assertEquals("stcat", ids[0])
    }

    @Test
    fun insert_duplicate_does_not_crash() = runTest {
        dao.insert(FavouriteEntity("stcat"))
        dao.insert(FavouriteEntity("stcat"))

        val ids = dao.getAllFavouriteIds()

        assertEquals(1, ids.size)
        assertEquals("stcat", ids[0])
    }

    @Test
    fun delete_removes_favourite() = runTest {
        dao.insert(FavouriteEntity("stcat"))
        dao.insert(FavouriteEntity("hcat"))

        dao.delete("stcat")

        val ids = dao.getAllFavouriteIds()
        assertEquals(1, ids.size)
        assertEquals("hcat", ids[0])
    }

    @Test
    fun getAllFavouriteIds_returns_all_ids() = runTest {
        dao.insert(FavouriteEntity("stcat"))
        dao.insert(FavouriteEntity("hcat"))
        dao.insert(FavouriteEntity("meow"))

        val ids = dao.getAllFavouriteIds()

        assertEquals(3, ids.size)
        assertTrue(ids.containsAll(listOf("stcat", "hcat", "meow")))
    }

    @Test
    fun isFavourite_returns_true_when_exists() = runTest {
        dao.insert(FavouriteEntity("stcat"))

        assertTrue(dao.isFavourite("stcat"))
    }

    @Test
    fun isFavourite_returns_false_when_not_exists() = runTest {
        assertFalse(dao.isFavourite("unknown"))
    }

    @Test
    fun getAllFavouriteIds_returns_empty_when_no_favourites() = runTest {
        val ids = dao.getAllFavouriteIds()

        assertTrue(ids.isEmpty())
    }
}
