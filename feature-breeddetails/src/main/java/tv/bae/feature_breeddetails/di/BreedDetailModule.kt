package tv.bae.feature_breeddetails.di

import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import tv.bae.feature_breeddetails.ui.BreedDetailViewModel

val breedDetailModule = module {
    viewModel { (breedId: String) -> BreedDetailViewModel(breedId, get(), get()) }
}
