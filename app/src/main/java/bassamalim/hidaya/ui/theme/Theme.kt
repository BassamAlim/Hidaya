package bassamalim.hidaya.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    surface = SurfaceM,
    onSurface = OnSurfaceM,
    primary = PrimaryM,
    onPrimary = OnPrimaryM
)

private val LightColorPalette = lightColors(
    surface = SurfaceL,
    onSurface = OnSurfaceM,
    primary = PrimaryL,
    onPrimary = OnPrimaryL
)

@Composable
fun HidayaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    if (darkTheme) systemUiController.setSystemBarsColor(color = PrimaryM)
    else systemUiController.setSystemBarsColor(color = PrimaryL)

    val colors =
        if (darkTheme) DarkColorPalette
        else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}