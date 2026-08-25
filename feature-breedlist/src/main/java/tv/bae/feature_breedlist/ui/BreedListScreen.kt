package tv.bae.feature_breedlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 5
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadNextPage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cat Breeds") })
        },
    ) { padding ->
        when (val state = uiState) {
            is BreedListUiState.Loading -> {
                LoadingState(modifier = modifier.padding(padding))
            }
            is BreedListUiState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.breeds, key = { it.id }) { breed ->
                        BreedCard(
                            name = breed.name,
                            imageUrl = breed.imageUrl,
                            isFavourite = breed.isFavourite,
                            onFavouriteClick = { viewModel.toggleFavourite(breed.id) },
                            onClick = { onBreedClick(breed.id) },
                        )
                    }
                }
            }
            is BreedListUiState.Empty -> {
                EmptyState(
                    message = "No breeds found",
                    modifier = modifier.padding(padding),
                )
            }
            is BreedListUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadNextPage() },
                    modifier = modifier.padding(padding),
                )
            }
        }
    }
}
