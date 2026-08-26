package tv.bae.feature_breedlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.bae.core.data.local.dao.FavouriteDao
import tv.bae.core.data.local.entities.BreedEntity
import tv.bae.core.domain.model.Breed
import tv.bae.core.domain.repository.BreedRepository
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class BreedListViewModel(
    breedRepository: BreedRepository,
    favouriteDao: FavouriteDao,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
) : ViewModel() {

    val breeds: Flow<PagingData<BreedEntity>> = breedRepository
        .getBreedsPager()
        .cachedIn(viewModelScope)

    val favouriteIds: StateFlow<Set<String>> = favouriteDao
        .getAllFavouriteIdsFlow()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet(),
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults: StateFlow<List<Breed>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                flowOf(breedRepository.searchBreeds(query).getOrDefault(emptyList()))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun toggleFavourite(breedId: String) {
        viewModelScope.launch {
            toggleFavouriteUseCase(breedId)
        }
    }
}
