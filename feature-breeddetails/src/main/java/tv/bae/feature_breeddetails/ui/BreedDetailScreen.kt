package tv.bae.feature_breeddetails.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.bae.designsystem.components.ErrorState
import tv.bae.designsystem.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedDetailScreen(
    breedId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BreedDetailViewModel = koinViewModel(parameters = { parametersOf(breedId) }),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breed Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            when (val state = uiState) {
                is BreedDetailUiState.Success -> {
                    FloatingActionButton(onClick = { viewModel.toggleFavourite() }) {
                        Icon(
                            imageVector = if (state.breed.isFavourite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = if (state.breed.isFavourite) {
                                "Remove from favourites"
                            } else {
                                "Add to favourites"
                            },
                        )
                    }
                }
                else -> {}
            }
        },
    ) { padding ->
        when (val state = uiState) {
            is BreedDetailUiState.Loading -> {
                LoadingState(modifier = modifier.padding(padding))
            }
            is BreedDetailUiState.Success -> {
                Column(
                    modifier = modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = state.breed.name,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    DetailSection(title = "Origin", content = state.breed.origin)
                    DetailSection(title = "Temperament", content = state.breed.temperament)
                    DetailSection(title = "Description", content = state.breed.description)
                }
            }
            is BreedDetailUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadBreed() },
                    modifier = modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
