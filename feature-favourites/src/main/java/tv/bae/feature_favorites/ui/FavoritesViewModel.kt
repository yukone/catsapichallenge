package tv.bae.feature_favorites.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.bae.core.domain.usecase.GetAverageLifespanUseCase
import tv.bae.core.domain.usecase.GetFavouriteBreedsUseCase
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

class FavoritesViewModel(
    private val getFavouriteBreedsUseCase: GetFavouriteBreedsUseCase,
    private val getAverageLifespanUseCase: GetAverageLifespanUseCase,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = FavoritesUiState.Loading
            getFavouriteBreedsUseCase()
                .onSuccess { breeds ->
                    val avgLifespan = getAverageLifespanUseCase().getOrNull()
                    _uiState.value = if (breeds.isEmpty()) {
                        FavoritesUiState.Empty
                    } else {
                        FavoritesUiState.Success(
                            breeds = breeds,
                            averageLifespan = avgLifespan,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = FavoritesUiState.Error(
                        e.message ?: "An unexpected error occurred",
                    )
                }
        }
    }

    fun removeFavourite(breedId: String) {
        viewModelScope.launch {
            toggleFavouriteUseCase(breedId)
            loadFavorites()
        }
    }
}
