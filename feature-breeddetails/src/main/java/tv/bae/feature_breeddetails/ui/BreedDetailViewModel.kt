package tv.bae.feature_breeddetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.bae.core.domain.usecase.GetBreedDetailUseCase
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

class BreedDetailViewModel(
    private val breedId: String,
    private val getBreedDetailUseCase: GetBreedDetailUseCase,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BreedDetailUiState>(BreedDetailUiState.Loading)
    val uiState: StateFlow<BreedDetailUiState> = _uiState.asStateFlow()

    init {
        loadBreed()
    }

    fun loadBreed() {
        viewModelScope.launch {
            _uiState.value = BreedDetailUiState.Loading
            getBreedDetailUseCase(breedId)
                .onSuccess { breed ->
                    _uiState.value = BreedDetailUiState.Success(breed)
                }
                .onFailure { e ->
                    _uiState.value = BreedDetailUiState.Error(
                        e.message ?: "An unexpected error occurred",
                    )
                }
        }
    }

    fun toggleFavourite() {
        viewModelScope.launch {
            toggleFavouriteUseCase(breedId)
            loadBreed()
        }
    }
}
