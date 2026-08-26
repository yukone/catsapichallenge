package tv.bae.feature_breedlist.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.bae.feature_breedlist.ui.BreedListViewModel

val breedListModule = module {
    viewModel { BreedListViewModel(get(), get(), get()) }
}
