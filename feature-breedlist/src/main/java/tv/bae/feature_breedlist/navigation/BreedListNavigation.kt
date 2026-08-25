package tv.bae.feature_breedlist.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import tv.bae.feature_breedlist.ui.BreedListScreen

@Serializable
data object BreedListRoute

fun NavGraphBuilder.breedListGraph(
    onBreedClick: (String) -> Unit,
) {
    composable<BreedListRoute> {
        BreedListScreen(onBreedClick = onBreedClick)
    }
}
