package bassamalim.hidaya.ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.tajwal

@Composable
fun MyText(
    text: String,
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
            lineHeight = fontSize * 1.4
        )
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
        )
    )
}