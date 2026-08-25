package tv.bae.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.bae.designsystem.theme.CatTheme

@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (onRetry != null) {
            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}

@Preview
@Composable
private fun ErrorStateWithRetryPreview() {
    CatTheme { ErrorState(message = "Something went wrong", onRetry = {}) }
}

@Preview
@Composable
private fun ErrorStateNoRetryPreview() {
    CatTheme { ErrorState(message = "Something went wrong") }
}
