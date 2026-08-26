package tv.bae.feature_breeddetails.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import tv.bae.feature_breeddetails.ui.BreedDetailScreen

@Serializable
data class BreedDetailRoute(val breedId: String)

fun NavController.navigateToBreedDetail(breedId: String) {
    navigate(BreedDetailRoute(breedId))
}

fun NavGraphBuilder.breedDetailGraph(
    onBackClick: () -> Unit,
) {
    composable<BreedDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<BreedDetailRoute>()
        BreedDetailScreen(
            breedId = route.breedId,
            onBackClick = onBackClick,
        )
    }
}
