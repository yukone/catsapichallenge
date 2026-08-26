package tv.bae.feature_favorites.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import tv.bae.designsystem.components.BreedCard
import tv.bae.designsystem.components.EmptyState
import tv.bae.designsystem.components.ErrorState
import tv.bae.designsystem.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBreedClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Favourites") })
        },
    ) { padding ->
        when (val state = uiState) {
            is FavoritesUiState.Loading -> {
                LoadingState(modifier = modifier.padding(padding))
            }
            is FavoritesUiState.Success -> {
                LazyColumn(
                    modifier = modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        LifespanSummaryCard(averageLifespan = state.averageLifespan)
                    }
                    items(state.breeds, key = { it.id }) { breed ->
                        BreedCard(
                            name = breed.name,
                            imageUrl = breed.imageUrl,
                            isFavourite = breed.isFavourite,
                            onFavouriteClick = { viewModel.removeFavourite(breed.id) },
                            onClick = { onBreedClick(breed.id) },
                        )
                    }
                }
            }
            is FavoritesUiState.Empty -> {
                EmptyState(
                    message = "No favourites yet",
                    modifier = modifier.padding(padding),
                )
            }
            is FavoritesUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadFavorites() },
                    modifier = modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun LifespanSummaryCard(averageLifespan: Double?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Average Lifespan",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (averageLifespan != null) {
                    "${String.format("%.1f", averageLifespan)} years"
                } else {
                    "N/A"
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
