package tv.bae.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = Cyan80,
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = Cyan40,
)

@Composable
fun CatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun CatThemeLightPreview() {
    CatTheme(darkTheme = false) {
        MaterialTheme.typography.bodyLarge.let {}
    }
}

@Preview(showBackground = true)
@Composable
private fun CatThemeDarkPreview() {
    CatTheme(darkTheme = true) {
        MaterialTheme.typography.bodyLarge.let {}
    }
}
