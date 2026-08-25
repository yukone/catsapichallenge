package tv.bae.feature_breedlist.ui

import tv.bae.core.domain.model.Breed

sealed interface BreedListUiState {
    data object Loading : BreedListUiState
    data class Success(val breeds: List<Breed>) : BreedListUiState
    data object Empty : BreedListUiState
    data class Error(val message: String) : BreedListUiState
}
