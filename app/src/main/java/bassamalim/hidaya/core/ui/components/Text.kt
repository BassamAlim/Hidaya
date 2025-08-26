package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.core.ui.theme.tajwal

@Composable
fun MyText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = Color.Unspecified,
    fontFamily: FontFamily = tajwal,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSize,
        style = TextStyle(
            fontFamily = fontFamily,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            lineHeight = fontSize * 1.4,
            platformStyle = PlatformTextStyle(includeFontPadding = true)
        ),
        softWrap = softWrap,
        maxLines = maxLines
    )
}

@Composable
fun MyText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Center,
    fontFamily: FontFamily = tajwal
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSize,
        style = TextStyle(
            fontFamily = fontFamily,
            color = textColor,
            fontWeight = fontWeight,
            textAlign = textAlign,
            lineHeight = fontSize * 1.4,
            platformStyle = PlatformTextStyle(
                includeFontPadding = true
            )
        )
    )
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MyText(
            message,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}