package tv.bae.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import tv.bae.designsystem.theme.CatTheme

@Composable
fun BreedCard(
    name: String,
    imageUrl: String?,
    isFavourite: Boolean,
    onFavouriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Card(
        onClick = { onClick?.invoke() },
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color.LightGray),
                error = ColorPainter(Color.Red),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            FavouriteButton(
                isFavourite = isFavourite,
                onClick = onFavouriteClick,
            )
        }
    }
}

@Preview
@Composable
private fun BreedCardPreview() {
    CatTheme {
        BreedCard(
            name = "Siamese",
            imageUrl = null,
            isFavourite = false,
            onFavouriteClick = {},
        )
    }
}

@Preview
@Composable
private fun BreedCardFavPreview() {
    CatTheme {
        BreedCard(
            name = "Maine Coon",
            imageUrl = null,
            isFavourite = true,
            onFavouriteClick = {},
        )
    }
}
