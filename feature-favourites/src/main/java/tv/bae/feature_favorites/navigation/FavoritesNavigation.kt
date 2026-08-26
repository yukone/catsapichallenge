package tv.bae.feature_favorites.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.bae.feature_favorites.ui.FavoritesScreen

@Serializable
data object FavoritesRoute

fun NavGraphBuilder.favoritesGraph(
    onBreedClick: (String) -> Unit,
) {
    composable<FavoritesRoute> {
        FavoritesScreen(onBreedClick = onBreedClick)
    }
}
