package bassamalim.hidaya.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import bassamalim.hidaya.core.enums.Theme

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography: AppTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val dimensions: AppDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current
}

@Composable
fun AppTheme(
    theme: Theme = Theme.DARK,
    typography: AppTypography = AppTheme.typography,
    dimensions: AppDimensions = AppTheme.dimensions,
    direction: LayoutDirection = LayoutDirection.Rtl,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalColors provides getColors(theme),
        LocalDimensions provides dimensions,
        LocalTypography provides typography,
        LocalLayoutDirection provides direction
    ) {
        content()
    }
}

private fun getColors(theme: Theme) = when (theme) {
    Theme.DARK -> darkColors()
    Theme.NIGHT -> nightColors()
    Theme.LIGHT -> lightColors()
}
