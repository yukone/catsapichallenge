package tv.bae.feature_favorites.ui

import tv.bae.core.domain.model.Breed

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Success(
        val breeds: List<Breed>,
        val averageLifespan: Double?,
    ) : FavoritesUiState
    data object Empty : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
}
