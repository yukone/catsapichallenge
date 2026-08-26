package tv.bae.catsapichallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tv.bae.designsystem.theme.CatTheme
import tv.bae.feature_breedlist.navigation.BreedListRoute
import tv.bae.feature_breedlist.navigation.breedListGraph
import tv.bae.feature_favorites.navigation.FavoritesRoute
import tv.bae.feature_favorites.navigation.favoritesGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatTheme {
                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarItems = listOf(
        BottomNavItem("Breeds", BreedListRoute, Icons.AutoMirrored.Filled.List),
        BottomNavItem("Favourites", FavoritesRoute, Icons.Default.Favorite),
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomBarItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == item.route::class.qualifiedName
                        } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BreedListRoute,
            modifier = Modifier.padding(padding),
        ) {
            breedListGraph(
                onBreedClick = { /* TODO: navigate to detail */ },
            )
            favoritesGraph(
                onBreedClick = { /* TODO: navigate to detail */ },
            )
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val route: Any,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
