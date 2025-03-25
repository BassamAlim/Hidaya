package bassamalim.hidaya.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.ThemeColor

@Composable
fun AppTheme(
    theme: Theme = Theme.ORIGINAL,
    direction: LayoutDirection = LayoutDirection.Rtl,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides direction
    ) {
        MaterialTheme(colorScheme = getColorScheme(theme)) {
            content()
        }
    }
}

fun getColorScheme(theme: Theme) = when (theme) {
    Theme.ORIGINAL -> originalColorScheme
    Theme.WHITE -> whiteColorScheme
    Theme.BLACK -> blackColorScheme
}

fun getThemeColor(color: ThemeColor, theme: Theme): Color {
    val colorScheme = getColorScheme(theme)

    return when (color) {
        ThemeColor.PRIMARY -> colorScheme.primary
        ThemeColor.ON_PRIMARY -> colorScheme.onPrimary
        ThemeColor.PRIMARY_CONTAINER -> colorScheme.primaryContainer
        ThemeColor.ON_PRIMARY_CONTAINER -> colorScheme.onPrimaryContainer
        ThemeColor.INVERSE_PRIMARY -> colorScheme.inversePrimary
        ThemeColor.SECONDARY -> colorScheme.secondary
        ThemeColor.ON_SECONDARY -> colorScheme.onSecondary
        ThemeColor.SECONDARY_CONTAINER -> colorScheme.secondaryContainer
        ThemeColor.ON_SECONDARY_CONTAINER -> colorScheme.onSecondaryContainer
        ThemeColor.TERTIARY -> colorScheme.tertiary
        ThemeColor.ON_TERTIARY -> colorScheme.onTertiary
        ThemeColor.TERTIARY_CONTAINER -> colorScheme.tertiaryContainer
        ThemeColor.ON_TERTIARY_CONTAINER -> colorScheme.onTertiaryContainer
        ThemeColor.BACKGROUND -> colorScheme.background
        ThemeColor.ON_BACKGROUND -> colorScheme.onBackground
        ThemeColor.SURFACE -> colorScheme.surface
        ThemeColor.ON_SURFACE -> colorScheme.onSurface
        ThemeColor.SURFACE_VARIANT -> colorScheme.surfaceVariant
        ThemeColor.ON_SURFACE_VARIANT -> colorScheme.onSurfaceVariant
        ThemeColor.SURFACE_TINT -> colorScheme.surfaceTint
        ThemeColor.INVERSE_SURFACE -> colorScheme.inverseSurface
        ThemeColor.INVERSE_ON_SURFACE -> colorScheme.inverseOnSurface
        ThemeColor.ERROR -> colorScheme.error
        ThemeColor.ON_ERROR -> colorScheme.onError
        ThemeColor.ERROR_CONTAINER -> colorScheme.errorContainer
        ThemeColor.ON_ERROR_CONTAINER -> colorScheme.onErrorContainer
        ThemeColor.OUTLINE -> colorScheme.outline
        ThemeColor.OUTLINE_VARIANT -> colorScheme.outlineVariant
        ThemeColor.SCRIM -> colorScheme.scrim
        ThemeColor.SURFACE_CONTAINER_HIGHEST -> colorScheme.surfaceContainerHighest
        ThemeColor.SURFACE_CONTAINER_HIGH -> colorScheme.surfaceContainerHigh
        ThemeColor.SURFACE_CONTAINER -> colorScheme.surfaceContainer
        ThemeColor.SURFACE_CONTAINER_LOW -> colorScheme.surfaceContainerLow
        ThemeColor.SURFACE_CONTAINER_LOWEST -> colorScheme.surfaceContainerLowest
        ThemeColor.SURFACE_BRIGHT -> colorScheme.surfaceBright
        ThemeColor.SURFACE_DIM -> colorScheme.surfaceDim
    }
}