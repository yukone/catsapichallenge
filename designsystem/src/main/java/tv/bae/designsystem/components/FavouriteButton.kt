package tv.bae.designsystem.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import tv.bae.designsystem.theme.CatTheme

@Composable
fun FavouriteButton(
    isFavourite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
        )
    }
}

@Preview
@Composable
private fun FavouriteButtonNotFavPreview() {
    CatTheme { FavouriteButton(isFavourite = false, onClick = {}) }
}

@Preview
@Composable
private fun FavouriteButtonFavPreview() {
    CatTheme { FavouriteButton(isFavourite = true, onClick = {}) }
}
