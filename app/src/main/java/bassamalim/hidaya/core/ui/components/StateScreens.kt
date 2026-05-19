package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import bassamalim.hidaya.core.ui.theme.appTypography
import bassamalim.hidaya.core.ui.theme.dimensions

/**
 * Centered empty-state placeholder: optional icon + message.
 * Use when a list/screen has no content and there is no error.
 */
@Composable
fun MyEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val dims = MaterialTheme.dimensions
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dims.spaceXl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dims.iconXl),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(dims.spaceLg))
        }
        Text(
            text = message,
            style = MaterialTheme.appTypography.subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Centered error-state placeholder: message + optional retry button.
 * Use when a screen failed to load data.
 */
@Composable
fun MyErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    retryLabel: String = "Retry",
) {
    val dims = MaterialTheme.dimensions
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dims.spaceXl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.appTypography.subtitle,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(Modifier.size(dims.spaceLg))
            Button(onClick = onRetry) {
                Text(text = retryLabel, style = MaterialTheme.appTypography.button)
            }
        }
    }
}
