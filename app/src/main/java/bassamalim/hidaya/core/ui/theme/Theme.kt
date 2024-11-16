package bassamalim.hidaya.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import bassamalim.hidaya.core.enums.Theme

//object AppTheme {
//    val colors: AppColors
//        @Composable
//        @ReadOnlyComposable
//        get() = LocalColors.current
//
//    val typography: AppTypography
//        @Composable
//        @ReadOnlyComposable
//        get() = LocalTypography.current
//
//    val dimensions: AppDimensions
//        @Composable
//        @ReadOnlyComposable
//        get() = LocalDimensions.current
//}

@Composable
fun AppTheme(
    theme: Theme = Theme.ORIGINAL,
//    typography: AppTypography = AppTheme.typography,
//    dimensions: AppDimensions = AppTheme.dimensions,
    direction: LayoutDirection = LayoutDirection.Rtl,
    content: @Composable () -> Unit
) {
//    CompositionLocalProvider(
//        LocalColors provides getColors(theme),
//        LocalDimensions provides dimensions,
//        LocalTypography provides typography,
//        LocalLayoutDirection provides direction
//    ) {
//        content()
//    }

    CompositionLocalProvider(
        LocalLayoutDirection provides direction
    ) {
        MaterialTheme(
            colorScheme = getColors(theme),
            content = content
        )
    }
}

//private fun getColors(theme: Theme) = when (theme) {
//    Theme.ORIGINAL -> darkColors()
//    Theme.WHITE -> lightColors()
//    Theme.NIGHT -> nightColors()
//    Theme.LIGHT -> lightColors()
//}

private fun getColors(theme: Theme) = when (theme) {
    Theme.ORIGINAL -> colorSchemeO
    Theme.WHITE -> colorSchemeW
    Theme.LIGHT -> colorSchemeW
    Theme.NIGHT -> colorSchemeO
}