package bassamalim.hidaya.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import bassamalim.hidaya.core.enums.Theme

@Composable
fun AppTheme(
    theme: Theme = Theme.ORIGINAL,
    direction: LayoutDirection = LayoutDirection.Rtl,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides direction
    ) {
        MaterialTheme(colorScheme = getColors(theme)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                content()
            }
        }
    }
}

private fun getColors(theme: Theme) = when (theme) {
    Theme.ORIGINAL -> colorSchemeO
    Theme.WHITE -> colorSchemeW
    Theme.LIGHT -> colorSchemeW
    Theme.NIGHT -> colorSchemeO
}