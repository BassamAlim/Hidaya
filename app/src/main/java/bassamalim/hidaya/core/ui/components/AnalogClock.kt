package bassamalim.hidaya.core.ui.components

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.TimeOfDay
import bassamalim.hidaya.core.ui.theme.AppTheme
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClock(
    previousPrayerTime: TimeOfDay?,
    nextPrayerTime: TimeOfDay?,
    numeralsLanguage: Language,
    modifier: Modifier = Modifier
) {
    val currentTime = TimeOfDay.fromCalendar(Calendar.getInstance())

    Draw(
        currentTime = currentTime,
        previousPrayerTime = previousPrayerTime,
        nextPrayerTime = nextPrayerTime,
        modifier = modifier,
        numeralsLanguage = numeralsLanguage
    )
}

@Composable
private fun Draw(
    currentTime: TimeOfDay,
    previousPrayerTime: TimeOfDay?,
    nextPrayerTime: TimeOfDay?,
    modifier: Modifier,
    numeralsLanguage: Language
) {
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()

    val teethColor = AppTheme.colors.text
    val numbersColor = AppTheme.colors.text
    val hoursHandColor = AppTheme.colors.text
    val minutesHandColor = AppTheme.colors.text
    val secondsHandColor = AppTheme.colors.accent
    val pastArcColor = AppTheme.colors.accent
    val remainingArcColor = AppTheme.colors.altAccent

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Canvas(
            modifier = Modifier.size(maxWidth)
        ) {
            val center = size.maxDimension / 2f
            val fullRadius = size.maxDimension / 2f

            drawTeeth(
                center = center,
                fullRadius = fullRadius,
                color = teethColor
            )

            drawNumbers(
                context = context,
                numeralsLanguage = numeralsLanguage,
                center = center,
                fullRadius = fullRadius,
                textMeasurer = textMeasurer,
                color = numbersColor
            )

            drawHands(
                currentTime = currentTime,
                center = center,
                fullRadius = fullRadius,
                hoursHandColor = hoursHandColor,
                minutesHandColor = minutesHandColor,
                secondsHandColor = secondsHandColor
            )

            drawArcs(
                previousPrayerTime = previousPrayerTime,
                nextPrayerTime = nextPrayerTime,
                center = center,
                fullRadius = fullRadius,
                pastArcColor = pastArcColor,
                remainingArcColor = remainingArcColor
            )
        }
    }
}

private fun DrawScope.drawTeeth(
    center: Float,
    fullRadius: Float,
    color: Color
) {
    val radius = fullRadius * 0.985f

    for (i in 0..59) {
        val theta = i * 0.105f  // 0.105 is the distance between each two teeth spaces
        drawLine(
            start = Offset(
                x = center + radius * cos(theta),
                y = center + radius * sin(theta)
            ),
            end = Offset(
                x = center + (radius + 10) * cos(theta),
                y = center + (radius + 10) * sin(theta)
            ),
            strokeWidth = 5f,
            color = color
        )
    }
}

private fun DrawScope.drawNumbers(
    context: Context,
    numeralsLanguage: Language,
    center: Float,
    fullRadius: Float,
    textMeasurer: TextMeasurer,
    color: Color
) {
    val numerals = when (numeralsLanguage) {
        Language.ARABIC -> context.resources.getStringArray(R.array.numerals)
        Language.ENGLISH -> context.resources.getStringArray(R.array.numerals_en)
    }
    val radius = fullRadius * 0.85f
    val textSize = radius * 0.07f

    for (number in numerals) {
        val angle = Math.PI / 6 * (number.toInt() - 3)
        val textStyle = TextStyle(fontSize = textSize.sp, color = color)
        val numberWidth = textMeasurer.measure(number, style = textStyle).size.width
        val numberHeight = textMeasurer.measure(number, style = textStyle).size.height

        drawText(
            textMeasurer = textMeasurer,
            text = number,
            topLeft = Offset(
                x = (center + cos(angle) * radius - numberWidth / 2).toFloat(),
                y = (center + sin(angle) * radius - numberHeight / 2).toFloat()
            ),
            style = textStyle
        )
    }
}

private fun DrawScope.drawHands(
    currentTime: TimeOfDay,
    center: Float,
    fullRadius: Float,
    hoursHandColor: Color,
    minutesHandColor: Color,
    secondsHandColor: Color
) {
    drawHoursHand(
        hour = currentTime.hour,
        minute = currentTime.minute,
        center = center,
        fullRadius = fullRadius,
        color = hoursHandColor
    )

    drawMinutesHand(
        minute = currentTime.minute,
        center = center,
        fullRadius = fullRadius,
        color = minutesHandColor
    )

    drawSecondsHand(
        second = currentTime.second,
        center = center,
        fullRadius = fullRadius,
        color = secondsHandColor
    )
}

private fun DrawScope.drawHoursHand(
    hour: Int,
    minute: Int,
    center: Float,
    fullRadius: Float,
    color: Color
) {
    val angle = (Math.PI * (hour + minute / 60f) * 5f / 30f - Math.PI / 2f).toFloat()
    val radius = fullRadius * 0.5f

    drawLine(
        start = Offset(
            x = center,
            y = center
        ),
        end = Offset(
            x = (center + cos(angle) * radius),
            y = (center + sin(angle) * radius)
        ),
        strokeWidth = 11f,
        cap = StrokeCap.Round,
        color = color
    )
}

private fun DrawScope.drawMinutesHand(
    minute: Int,
    center: Float,
    fullRadius: Float,
    color: Color
) {
    val angle = (Math.PI * minute / 30f - Math.PI / 2f).toFloat()
    val radius = fullRadius * 0.7f

    drawLine(
        start = Offset(
            x = center,
            y = center
        ),
        end = Offset(
            x = (center + cos(angle) * radius),
            y = (center + sin(angle) * radius)
        ),
        strokeWidth = 9f,
        cap = StrokeCap.Round,
        color = color
    )
}

private fun DrawScope.drawSecondsHand(
    second: Int,
    center: Float,
    fullRadius: Float,
    color: Color
) {
    val angle = (Math.PI * second * 5f / 30f - Math.PI / 2f).toFloat()
    val radius = fullRadius * 0.7f

    drawLine(
        start = Offset(
            x = center,
            y = center
        ),
        end = Offset(
            x = (center + cos(angle) * radius),
            y = (center + sin(angle) * radius)
        ),
        strokeWidth = 6f,
        cap = StrokeCap.Round,
        color = color
    )
}

private fun DrawScope.drawArcs(
    previousPrayerTime: TimeOfDay?,
    nextPrayerTime: TimeOfDay?,
    center: Float,
    fullRadius: Float,
    pastArcColor: Color,
    remainingArcColor: Color
) {
    if (previousPrayerTime != null) {
        drawPassedArc(
            previousPrayerTime = previousPrayerTime,
            center = center,
            fullRadius = fullRadius,
            color = pastArcColor
        )
    }

    if (nextPrayerTime != null) {
        drawRemainingArc(
            nextPrayerTime = nextPrayerTime,
            center = center,
            fullRadius = fullRadius,
            color = remainingArcColor
        )
    }
}

private fun DrawScope.drawPassedArc(
    previousPrayerTime: TimeOfDay?,
    center: Float,
    fullRadius: Float,
    color: Color
) {

}

private fun DrawScope.drawRemainingArc(
    nextPrayerTime: TimeOfDay?,
    center: Float,
    fullRadius: Float,
    color: Color
) {

}