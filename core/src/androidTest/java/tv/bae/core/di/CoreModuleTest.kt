package tv.bae.core.di

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.ktor.client.engine.HttpClientEngine
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.verify.verify

@RunWith(AndroidJUnit4::class)
class CoreModuleTest : KoinTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun coreModule_declarations_are_all_resolvable() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(coreModule)
        }
        coreModule.verify(extraTypes = listOf(HttpClientEngine::class))
        stopKoin()
    }
}
