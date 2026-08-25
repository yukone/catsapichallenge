package tv.bae.catsapichallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import tv.bae.designsystem.theme.CatTheme
import tv.bae.feature_breedlist.navigation.BreedListRoute
import tv.bae.feature_breedlist.navigation.breedListGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = BreedListRoute,
                ) {
                    breedListGraph(
                        onBreedClick = { /* TODO: navigate to detail */ },
                    )
                }
            }
        }
    }
}
