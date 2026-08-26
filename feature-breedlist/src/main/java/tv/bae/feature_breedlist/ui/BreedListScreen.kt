package tv.bae.feature_breedlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import org.koin.androidx.compose.koinViewModel
import tv.bae.designsystem.components.BreedCard
import tv.bae.designsystem.components.EmptyState
import tv.bae.designsystem.components.ErrorState
import tv.bae.designsystem.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedListScreen(
    onBreedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BreedListViewModel = koinViewModel(),
) {
    val breeds = viewModel.breeds.collectAsLazyPagingItems()
    val favouriteIds by viewModel.favouriteIds.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    val isSearching = searchQuery.isNotBlank()

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSearch() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { isSearchActive = false },
                    expanded = isSearchActive,
                    onExpandedChange = { isSearchActive = it },
                    placeholder = { Text("Search breeds...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (isSearchActive) {
                            IconButton(onClick = {
                                viewModel.clearSearch()
                                isSearchActive = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    },
                )
            },
            expanded = isSearchActive,
            onExpandedChange = { isSearchActive = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isSearching) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (searchResults.isEmpty()) {
                        item {
                            EmptyState(message = "No breeds match your search")
                        }
                    } else {
                        items(searchResults.size) { index ->
                            val breed = searchResults[index]
                            BreedCard(
                                name = breed.name,
                                imageUrl = breed.imageUrl,
                                isFavourite = breed.id in favouriteIds,
                                onFavouriteClick = { viewModel.toggleFavourite(breed.id) },
                                onClick = {
                                    onBreedClick(breed.id)
                                    isSearchActive = false
                                },
                            )
                        }
                    }
                }
            }
        }

        if (!isSearching) {
            val refreshLoadState = breeds.loadState.refresh

            when {
                refreshLoadState is LoadState.Loading && breeds.itemCount == 0 -> {
                    LoadingState()
                }
                refreshLoadState is LoadState.Error && breeds.itemCount == 0 -> {
                    ErrorState(
                        message = refreshLoadState.error.message ?: "An unexpected error occurred",
                        onRetry = { breeds.retry() },
                    )
                }
                breeds.itemCount == 0 && refreshLoadState !is LoadState.Loading -> {
                    EmptyState(message = "No breeds found")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(breeds.itemCount) { index ->
                            val breed = breeds[index]
                            if (breed != null) {
                                BreedCard(
                                    name = breed.name,
                                    imageUrl = breed.imageUrl,
                                    isFavourite = breed.id in favouriteIds,
                                    onFavouriteClick = { viewModel.toggleFavourite(breed.id) },
                                    onClick = { onBreedClick(breed.id) },
                                )
                            }
                        }

                        if (breeds.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    LoadingState()
                                }
                            }
                        }

                        if (breeds.loadState.append is LoadState.Error) {
                            item {
                                ErrorState(
                                    message = "Failed to load more breeds",
                                    onRetry = { breeds.retry() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
