package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.tajwal

@Composable
fun MyText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Center,
    textColor: Color = AppTheme.colors.text,
    fontFamily: FontFamily = tajwal,
    softWrap: Boolean = true
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
        ),
        softWrap = softWrap
    )
}

@Composable
fun MyText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    textColor: Color = AppTheme.colors.text,
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
fun MyClickableText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    textColor: Color = AppTheme.colors.accent,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Center,
    fontFamily: FontFamily = tajwal,
    innerPadding: PaddingValues = PaddingValues(vertical = 6.dp, horizontal = 15.dp),
    onClick: () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(innerPadding),
            fontSize = fontSize,
            style = TextStyle(
                fontFamily = fontFamily,
                color = textColor,
                fontWeight = fontWeight,
                textAlign = textAlign,
                lineHeight = fontSize * 1.4
            )
        )
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MyText(
            message,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}