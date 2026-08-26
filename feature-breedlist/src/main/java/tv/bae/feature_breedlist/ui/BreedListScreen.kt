package tv.bae.feature_breedlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import org.koin.androidx.compose.koinViewModel
import tv.bae.designsystem.components.BreedCard
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cat Breeds") })
        },
    ) { padding ->
        val refreshLoadState = breeds.loadState.refresh

        when {
            refreshLoadState is LoadState.Loading && breeds.itemCount == 0 -> {
                LoadingState(modifier = modifier.padding(padding))
            }
            refreshLoadState is LoadState.Error && breeds.itemCount == 0 -> {
                ErrorState(
                    message = refreshLoadState.error.message ?: "An unexpected error occurred",
                    onRetry = { breeds.retry() },
                    modifier = modifier.padding(padding),
                )
            }
            breeds.itemCount == 0 && refreshLoadState !is LoadState.Loading -> {
                tv.bae.designsystem.components.EmptyState(
                    message = "No breeds found",
                    modifier = modifier.padding(padding),
                )
            }
            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
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
                                    .fillMaxSize()
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
