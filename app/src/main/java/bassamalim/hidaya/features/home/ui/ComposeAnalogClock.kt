package bassamalim.hidaya.features.home.ui

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.ui.theme.AppTheme
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ComposeAnalogClock(
    timeFromPreviousPrayer: Long,
    timeToNextPrayer: Long,
    numeralsLanguage: Language,
    modifier: Modifier = Modifier
) {
    val numerals = when (numeralsLanguage) {
        Language.ARABIC -> stringArrayResource(R.array.numerals)
        Language.ENGLISH -> stringArrayResource(R.array.numerals_en)
    }

    val now = Calendar.getInstance()

    val textMeasurer = rememberTextMeasurer()

    val bgColor = AppTheme.colors.background
    val linesColor = AppTheme.colors.onSurface
    val accentColor = AppTheme.colors.accent
    val accentDarkColor = AppTheme.colors.altAccent

    Canvas(
        modifier.fillMaxSize()
    ) {
        println("Canvas size: ${size.width}, ${size.height}")
        val canvasSize = min(size.width, size.height) * 0.9F
        val center = canvasSize / 2F
        val radius = canvasSize / 2F
        val teethR = radius * 0.985F
        val numeralsR = radius * 0.85F
        val hourHandR = radius * 0.5F
        val minuteHandR = radius * 0.7F
        val secondHandR = radius * 0.7F
        val textSize = when (numeralsLanguage) {
            Language.ARABIC -> canvasSize * 0.04F
            Language.ENGLISH -> canvasSize * 0.03F
        }
        now.timeInMillis = System.currentTimeMillis()

        drawTeeth(
            center = center,
            teethR = teethR,
            color = linesColor
        )

        drawNumerals(
            numerals = numerals,
            textMeasurer = textMeasurer,
            textSize = textSize,
            center = center,
            numeralsR = numeralsR,
            color = linesColor
        )

        drawHands(
            center = center,
            now = now,
            hourHandR = hourHandR,
            minuteHandR = minuteHandR,
            secondHandR = secondHandR,
            hourHandColor = linesColor,
            minuteHandColor = linesColor,
            secondHandColor = accentColor
        )

        if (timeFromPreviousPrayer != -1L)
            drawPassedArc(
                center = center,
                timeFromPreviousPrayer = timeFromPreviousPrayer
            )

        drawRemainingArc(
            center = center,
            timeToNextPrayer = timeToNextPrayer,
            now = now
        )

//        postInvalidateDelayed(500)
    }
}

private fun DrawScope.drawTeeth(center: Float, teethR: Float, color: Color) {
    for (i in 0..59) {
        val theta = i * 0.105F  // 0.105 is the distance between each two teeth spaces
        drawLine(
            color = color,
            start = Offset(center + teethR * cos(theta), center + teethR * sin(theta)),
            end = Offset(
                center + (teethR + 10) * cos(theta),
                center + (teethR + 10) * sin(theta)
            ),
            strokeWidth = 5F
        )
    }
}

private fun DrawScope.drawNumerals(
    numerals: Array<String>,
    textMeasurer: TextMeasurer,
    textSize: Float,
    center: Float,
    numeralsR: Float,
    color: Color
) {
    for (number in numerals) {
        val angle = Math.PI / 6 * (number.toInt() - 3)
        drawText(
            textMeasurer = textMeasurer,
            text = number,
            topLeft = Offset(
                x = (center + cos(angle) * numeralsR - angle / 2).toFloat(),
                y = (center + sin(angle) * numeralsR + angle / 2).toFloat()
            ),
            style = TextStyle(
                color = color,
                fontSize = textSize.sp
            )
        )
    }
}

private fun DrawScope.drawHands(
    center: Float,
    now: Calendar,
    hourHandR: Float,
    minuteHandR: Float,
    secondHandR: Float,
    hourHandColor: Color,
    minuteHandColor: Color,
    secondHandColor: Color
) {
    val time = getTime(now)

    drawHourHand(
        location = (time[0] + time[1] / 60F) * 5,
        center = center,
        hourHandR = hourHandR,
        color = hourHandColor
    )

    drawMinuteHand(
        location = time[1],
        center = center,
        minuteHandR = minuteHandR,
        color = minuteHandColor
    )

    drawSecondsHand(
        location = now.get(Calendar.SECOND).toFloat(),
        center = center,
        secondHandR = secondHandR,
        color = secondHandColor
    )
}

private fun DrawScope.drawHourHand(
    location: Float,
    center: Float,
    hourHandR: Float,
    color: Color
) {
    val theta = getHandTheta(location)
    drawLine(
        color = color,
        start = Offset((center - cos(theta) * 20).toFloat(), (center - sin(theta) * 20).toFloat()),
        end = Offset((center + cos(theta) * hourHandR).toFloat(), (center + sin(theta) * hourHandR).toFloat()),
        strokeWidth = 11F,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawMinuteHand(
    location: Float,
    center: Float,
    minuteHandR: Float,
    color: Color
) {
    val theta = getHandTheta(location)
    drawLine(
        color = color,
        start = Offset((center - cos(theta) * 30).toFloat(), (center - sin(theta) * 30).toFloat()),
        end = Offset((center + cos(theta) * minuteHandR).toFloat(), (center + sin(theta) * minuteHandR).toFloat()),
        strokeWidth = 9F,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawSecondsHand(
    location: Float,
    center: Float,
    secondHandR: Float,
    color: Color
) {
    val theta = getHandTheta(location)
    drawLine(
        color = color,
        start = Offset((center - cos(theta) * 40).toFloat(), (center - sin(theta) * 50).toFloat()),
        end = Offset((center + cos(theta) * secondHandR).toFloat(), (center + sin(theta) * secondHandR).toFloat()),
        strokeWidth = 6F,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawPassedArc(
    center: Float,
    timeFromPreviousPrayer: Long
) {
    val strokeWidth = 10

    val start = strokeWidth / 2
    val end = 2 * center - start
    val rect = RectF(start.toFloat(), start.toFloat(), end, end)

    val fromTheta = timeFromPreviousPrayer / 1000 / 60 / 2F
    val thetaDiff = (System.currentTimeMillis() - timeFromPreviousPrayer) / 1000 / 60 / 2F

//    canvas.drawArc(rect, fromTheta, thetaDiff, false, passedArcPaint)
}

private fun DrawScope.drawRemainingArc(
    center: Float,
    timeToNextPrayer: Long,
    now: Calendar
) {
    val strokeWidth = 10

    val start = strokeWidth / 2
    val end = 2 * center - start
    val rect = RectF(start.toFloat(), start.toFloat(), end, end)

    val time = getTime(now)

    val nowTheta = -90 + 30 * (time[0] + time[1] / 60F)
    val thetaDiff = timeToNextPrayer / 1000 / 60 / 2F

//    canvas.drawArc(rect, nowTheta, thetaDiff, false, secondHandPaint)
}

private fun getHandTheta(location: Float) = Math.PI * location / 30 - Math.PI / 2

/**
 * It returns an array of two floats, the hour and the minute
 *
 * @return An array of floats.
 */
fun getTime(now: Calendar): Array<Float> {
    var hour = now[Calendar.HOUR_OF_DAY].toFloat()
    hour = if (hour > 12) hour - 12 else hour  //convert to 12hour format from 24 hour format
    val minute = now[Calendar.MINUTE].toFloat()
    return arrayOf(hour, minute)
}