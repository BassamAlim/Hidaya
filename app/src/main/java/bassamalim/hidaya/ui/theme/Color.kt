package bassamalim.hidaya.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Grey = Color(0xFF484849)
val Positive = Color(0xFF1BA739)

// Theme L (Light)
val BGL = Color(0xFF8D99AE)
val PrimaryL = Color(0xFF293241)
val PrimaryWeakL = Color(0xB3293241)
val SurfaceL = Color(0xFF7E899C)
val RippleL = Color(0xFF99A5BC)
val AccentL = Color(0xFF14213D)
val AltAccentL = Color(0xFF1C2E55)
val TextL = Color(0xFF000000)
val WeakTextL = Color(0xFF414040)
val OnPrimaryL = Color(0xFFBCC2C7)
val HighlightL = Color(0xFF01849C)
val TrackL = Color(0xFF113297)
val QBGL = Color(0xFFBACDE1)
val SecondaryL = Color(0xFFD18E21)
val AltSecondaryL = Color(0xFFAA7113)
val ShadowL = Color(0xFF586678)

// Theme M (Dark)
val BGM = Color(0xFF1A2027)
val PrimaryM = Color(0xFF222831)
val PrimaryWeakM = Color(0xB3222831)
val OnPrimaryM = Color(0xFFBCC2C7)
val SurfaceM = Color(0xFF222D37)
val AltSurfaceM = Color(0xFF2D3B47)
val OnSurfaceM = Color(0xFF959A9E)
val RippleM = Color(0xFF496278)
val AccentM = Color(0xFF00ADB5)
val AltAccentM = Color(0xFF006B70)
val TextM = Color(0xFFBCC2C7)
val WeakTextM = Color(0xFFACB1B6)
val HighlightM = Color(0xFF00ADB5)
val TrackM = Color(0xFF48C986)
val ShadowM = Color(0xFF586678)

// Theme N
val BGN = Color(0xFF101010)
val PrimaryN = Color(0xFF141517)
val PrimaryWeakN = Color(0xB3323232)
val OnPrimaryN = Color(0xFF000000)
val SurfaceN = Color(0xFF141517)
val OnSurfaceN = Color(0xFFBCC2C7)
val AccentN = Color(0xFF0097AA)
val AltAccentN = Color(0xFF037583)
val RippleN = Color(0xFF3B3E43)
val TextN = Color(0xFFBCC2C7)
val WeakTextN = Color(0xFFACB1B6)
val HighlightN = Color(0xFF01849C)
val TrackN = Color(0xFF113297)
val QBGN = Color(0xFFBACDE1)
val ShadowN = Color(0xFF343D47)

class AppColors(
    background: Color,
    surface: Color,
    altSurface: Color,
    onSurface: Color,
    primary: Color,
    weakPrimary: Color,
    onPrimary: Color,
    secondary: Color,
    onSecondary: Color,
    accent: Color,
    altAccent: Color,
    text: Color,
    strongText: Color,
    weakText: Color,
    ripple: Color,
    highlight: Color,
    shadow: Color,
    track: Color
) {
    var background by mutableStateOf(background)
        private set
    var surface by mutableStateOf(surface)
        private set
    var altSurface by mutableStateOf(altSurface)
        private set
    var onSurface by mutableStateOf(onSurface)
        private set
    var primary by mutableStateOf(primary)
        private set
    var weakPrimary by mutableStateOf(weakPrimary)
        private set
    var onPrimary by mutableStateOf(onPrimary)
        private set
    var secondary by mutableStateOf(secondary)
        private set
    var onSecondary by mutableStateOf(onSecondary)
        private set
    var accent by mutableStateOf(accent)
        private set
    var altAccent by mutableStateOf(altAccent)
        private set
    var text by mutableStateOf(text)
        private set
    var strongText by mutableStateOf(strongText)
        private set
    var weakText by mutableStateOf(weakText)
        private set
    var ripple by mutableStateOf(ripple)
        private set
    var highlight by mutableStateOf(highlight)
        private set
    var shadow by mutableStateOf(shadow)
        private set
    var track by mutableStateOf(track)
        private set
}

fun lightColors(): AppColors = AppColors(
    background = BGL,
    surface = SurfaceL,
    altSurface = SurfaceL,
    onSurface = OnSurfaceM,
    primary = PrimaryL,
    weakPrimary = PrimaryWeakL,
    onPrimary = OnPrimaryL,
    secondary = SecondaryL,
    onSecondary = AltSecondaryL,
    accent = AccentL,
    altAccent = AltAccentL,
    text = TextL,
    strongText = Black,
    weakText = WeakTextL,
    ripple = RippleL,
    highlight = HighlightL,
    shadow = ShadowL,
    track = TrackL
)

fun darkColors(): AppColors = AppColors(
    background = BGM,
    surface = SurfaceM,
    altSurface = AltSurfaceM,
    onSurface = OnSurfaceM,
    primary = PrimaryM,
    weakPrimary = PrimaryWeakM,
    onPrimary = OnPrimaryM,
    secondary = AccentM,
    onSecondary = AltAccentM,
    accent = AccentM,
    altAccent = AltAccentM,
    text = TextM,
    strongText = White,
    weakText = WeakTextM,
    ripple = RippleM,
    highlight = HighlightM,
    shadow = ShadowM,
    track = TrackM
)

fun nightColors(): AppColors = AppColors(
    background = BGN,
    surface = SurfaceN,
    altSurface = SurfaceN,
    onSurface = OnSurfaceN,
    primary = PrimaryN,
    weakPrimary = PrimaryWeakN,
    onPrimary = OnPrimaryN,
    secondary = AccentN,
    onSecondary = AltAccentN,
    accent = AccentN,
    altAccent = AltAccentN,
    text = TextN,
    strongText = White,
    weakText = WeakTextN,
    ripple = RippleN,
    highlight = HighlightN,
    shadow = ShadowN,
    track = TrackN
)

val LocalColors = staticCompositionLocalOf { lightColors() }