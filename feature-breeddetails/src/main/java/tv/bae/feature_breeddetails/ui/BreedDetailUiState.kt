package tv.bae.feature_breeddetails.ui

import tv.bae.core.domain.model.Breed

sealed interface BreedDetailUiState {
    data object Loading : BreedDetailUiState
    data class Success(val breed: Breed) : BreedDetailUiState
    data class Error(val message: String) : BreedDetailUiState
}
