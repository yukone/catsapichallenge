package tv.bae.catsapichallenge

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import tv.bae.core.di.coreModule
import tv.bae.feature_breedlist.di.breedListModule
import tv.bae.feature_favorites.di.favoritesModule
import tv.bae.feature_breeddetails.di.breedDetailModule

class CatsApiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@CatsApiApplication)
            modules(
                coreModule,
                breedListModule,
                favoritesModule,
                breedDetailModule,
            )
        }
    }
}
