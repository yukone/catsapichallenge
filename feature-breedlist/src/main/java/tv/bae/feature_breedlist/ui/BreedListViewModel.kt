package tv.bae.feature_breedlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.usecase.GetBreedsUseCase
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

class BreedListViewModel(
    private val getBreedsUseCase: GetBreedsUseCase,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BreedListUiState>(BreedListUiState.Loading)
    val uiState: StateFlow<BreedListUiState> = _uiState.asStateFlow()

    private val allBreeds = mutableListOf<Breed>()
    private var currentPage = 0
    private var isLoadingMore = false
    private var reachedEnd = false

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoadingMore || reachedEnd) return
        isLoadingMore = true
        viewModelScope.launch {
            if (currentPage == 0) {
                _uiState.value = BreedListUiState.Loading
            }
            getBreedsUseCase(page = currentPage)
                .onSuccess { breeds ->
                    if (breeds.isEmpty()) {
                        reachedEnd = true
                    } else {
                        allBreeds.addAll(breeds)
                        currentPage++
                    }
                    _uiState.value = if (allBreeds.isEmpty()) {
                        BreedListUiState.Empty
                    } else {
                        BreedListUiState.Success(allBreeds.toList())
                    }
                }
                .onFailure { e ->
                    if (allBreeds.isEmpty()) {
                        _uiState.value = BreedListUiState.Error(
                            e.message ?: "An unexpected error occurred",
                        )
                    }
                }
            isLoadingMore = false
        }
    }

    fun toggleFavourite(breedId: String) {
        viewModelScope.launch {
            toggleFavouriteUseCase(breedId)
            currentPage = 0
            allBreeds.clear()
            reachedEnd = false
            loadNextPage()
        }
    }

    fun refreshIfNeeded() {
        if (allBreeds.isNotEmpty()) {
            viewModelScope.launch {
                val result = getBreedsUseCase(page = 0)
                result.onSuccess { breeds ->
                    val favIds = breeds.filter { it.isFavourite }.map { it.id }.toSet()
                    allBreeds.forEachIndexed { index, breed ->
                        allBreeds[index] = breed.copy(isFavourite = breed.id in favIds)
                    }
                    _uiState.value = BreedListUiState.Success(allBreeds.toList())
                }
            }
        }
    }
}
