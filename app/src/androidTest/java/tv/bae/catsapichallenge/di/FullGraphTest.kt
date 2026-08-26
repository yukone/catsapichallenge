package tv.bae.catsapichallenge.di

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.ktor.client.engine.HttpClientEngine
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.verify.verify
import tv.bae.core.di.coreModule
import tv.bae.feature_breedlist.di.breedListModule
import tv.bae.feature_breeddetails.di.breedDetailModule
import tv.bae.feature_favorites.di.favoritesModule

@RunWith(AndroidJUnit4::class)
class FullGraphTest : KoinTest {

    @After
    fun tearDown() {
        stopKoin()
    }

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun allModules_are_wired_correctly() {
        stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(coreModule, breedListModule, favoritesModule, breedDetailModule)
        }
        coreModule.verify(extraTypes = listOf(HttpClientEngine::class))
    }
}
