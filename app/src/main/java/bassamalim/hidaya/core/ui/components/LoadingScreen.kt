package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun MyCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.accent
) {
    CircularProgressIndicator(
        color = color,
        modifier = modifier
    )
}

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.accent
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color
        )
    }
}