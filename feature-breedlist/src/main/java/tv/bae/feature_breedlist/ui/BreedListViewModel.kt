package tv.bae.feature_breedlist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tv.bae.core.data.local.dao.FavouriteDao
import tv.bae.core.data.local.entities.BreedEntity
import tv.bae.core.domain.repository.BreedRepository
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

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

    fun toggleFavourite(breedId: String) {
        viewModelScope.launch {
            toggleFavouriteUseCase(breedId)
        }
    }
}
