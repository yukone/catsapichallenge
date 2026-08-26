package tv.bae.feature_favorites.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.bae.feature_favorites.ui.FavoritesViewModel

val favoritesModule = module {
    viewModel { FavoritesViewModel(get(), get(), get()) }
}
