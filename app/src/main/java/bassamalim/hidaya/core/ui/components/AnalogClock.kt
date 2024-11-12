package bassamalim.hidaya.core.ui.components

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val screenMin = minOf(screenWidth, screenHeight)
    val size = screenMin * 0.9f
    val center = size / 2f
    val fullRadius = size / 2f

    val teethColor = AppTheme.colors.text
    val numbersColor = AppTheme.colors.text
    val hoursHandColor = AppTheme.colors.text
    val minutesHandColor = AppTheme.colors.text
    val secondsHandColor = AppTheme.colors.accent
    val pastArcColor = AppTheme.colors.accent
    val remainingArcColor = AppTheme.colors.altAccent

    Box(
        modifier = modifier.size(size.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawTeeth(
                center = center,
                fullRadius = fullRadius,
                color = teethColor
            )

            drawNumbers(
                context = context,
                numeralsLanguage = numeralsLanguage,
                color = numbersColor
            )

            drawHands(
                currentTime = currentTime,
                hoursHandColor = hoursHandColor,
                minutesHandColor = minutesHandColor,
                secondsHandColor = secondsHandColor
            )

            drawArcs(
                previousPrayerTime = previousPrayerTime,
                nextPrayerTime = nextPrayerTime,
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
    val radius = fullRadius * 0.985F

    for (i in 0..59) {
        val theta = i * 0.105F  // 0.105 is the distance between each two teeth spaces
        drawLine(
            color = color,
            start = Offset(
                x = center + radius * cos(theta),
                y = center + radius * sin(theta)
            ),
            end = Offset(
                x = center + (radius + 10) * cos(theta),
                y = center + (radius + 10) * sin(theta)
            ),
            strokeWidth = 5F
        )
    }
}

private fun DrawScope.drawNumbers(
    context: Context,
    numeralsLanguage: Language,
    color: Color
) {
    val numerals = when (numeralsLanguage) {
        Language.ARABIC -> context.resources.getStringArray(R.array.numerals)
        Language.ENGLISH -> context.resources.getStringArray(R.array.numerals_en)
    }
}

private fun DrawScope.drawHands(
    currentTime: TimeOfDay,
    hoursHandColor: Color,
    minutesHandColor: Color,
    secondsHandColor: Color
) {
    drawHoursHand(
        hour = currentTime.hour,
        minute = currentTime.minute,
        color = hoursHandColor
    )

    drawMinutesHand(
        minute = currentTime.minute,
        color = minutesHandColor
    )

    drawSecondsHand(
        second = currentTime.second,
        color = secondsHandColor
    )
}

private fun DrawScope.drawHoursHand(
    hour: Int,
    minute: Int,
    color: Color
) {

}

private fun DrawScope.drawMinutesHand(
    minute: Int,
    color: Color
) {

}

private fun DrawScope.drawSecondsHand(
    second: Int,
    color: Color
) {

}

private fun DrawScope.drawArcs(
    previousPrayerTime: TimeOfDay?,
    nextPrayerTime: TimeOfDay?,
    pastArcColor: Color,
    remainingArcColor: Color
) {
    if (previousPrayerTime != null) {
        drawPassedArc(
            previousPrayerTime = previousPrayerTime,
            color = pastArcColor
        )
    }

    if (nextPrayerTime != null) {
        drawRemainingArc(
            nextPrayerTime = nextPrayerTime,
            color = remainingArcColor
        )
    }
}

private fun DrawScope.drawPassedArc(
    previousPrayerTime: TimeOfDay?,
    color: Color
) {

}

private fun DrawScope.drawRemainingArc(
    nextPrayerTime: TimeOfDay?,
    color: Color
) {

}

private fun timeToAngle(time: TimeOfDay): Float {
    val hour = time.hour % 12
    val minute = time.minute
    return (hour * 30f + minute * 0.5f) // 30 degrees per hour, 0.5 degrees per minute
}