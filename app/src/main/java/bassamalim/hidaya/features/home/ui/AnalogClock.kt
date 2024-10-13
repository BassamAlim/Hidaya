package bassamalim.hidaya.features.home.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AnalogClock(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    private val now = Calendar.getInstance()
    private var center = 0F
    private var teethR = 0F
    private var numeralsR = 0F
    private var hourHandR = 0F
    private var minuteHandR = 0F
    private var secondHandR = 0F
    private var mRect = Rect()
    private val numeralsPaint = Paint()
    private val teethPaint = Paint()
    private val hourHandPaint = Paint()
    private val minuteHandPaint = Paint()
    private val secondHandPaint = Paint()
    private val passedArcPaint = Paint()
    private lateinit var numeralsLanguage: Language
    private lateinit var numerals: Array<String>
    private val bgColor = TypedValue()
    private val linesColor = TypedValue()
    private val accentColor = TypedValue()
    private val accentDarkColor = TypedValue()
    private var timeFromPreviousPrayer = 0L
    private var timeToNextPrayer = 0L

    fun init(numeralsLanguage: Language) {
        setupPaint()

        this.numeralsLanguage = numeralsLanguage
        numerals = when (numeralsLanguage) {
            Language.ARABIC -> context.resources.getStringArray(R.array.numerals)
            Language.ENGLISH -> context.resources.getStringArray(R.array.numerals_en)
        }
    }

    private fun setupPaint() {
        context.theme.resolveAttribute(R.attr.myMainBg, bgColor, true)
        context.theme.resolveAttribute(R.attr.myText, linesColor, true)
        context.theme.resolveAttribute(R.attr.myAccent, accentColor, true)
        context.theme.resolveAttribute(R.attr.myAccentDark, accentDarkColor, true)

        // Teeth
        teethPaint.style = Paint.Style.STROKE
        teethPaint.color = linesColor.data
        teethPaint.strokeWidth = 5F
        teethPaint.isAntiAlias = true

        // Numerals
        numeralsPaint.style = Paint.Style.FILL
        numeralsPaint.color = linesColor.data
        numeralsPaint.isAntiAlias = true

        // Hour hand
        hourHandPaint.style = Paint.Style.STROKE
        hourHandPaint.color = linesColor.data
        hourHandPaint.strokeWidth = 11F
        hourHandPaint.strokeCap = Paint.Cap.ROUND
        hourHandPaint.isAntiAlias = true

        // Minute hand
        minuteHandPaint.style = Paint.Style.STROKE
        minuteHandPaint.color = linesColor.data
        minuteHandPaint.strokeWidth = 9F
        minuteHandPaint.strokeCap = Paint.Cap.ROUND
        minuteHandPaint.isAntiAlias = true

        // Second hand
        secondHandPaint.style = Paint.Style.STROKE
        secondHandPaint.color = accentColor.data
        secondHandPaint.strokeWidth = 6F
        secondHandPaint.strokeCap = Paint.Cap.ROUND
        secondHandPaint.isAntiAlias = true

        // Between prayers arc
        passedArcPaint.style = Paint.Style.STROKE
        passedArcPaint.color = accentDarkColor.data
        passedArcPaint.strokeWidth = 6F
        passedArcPaint.strokeCap = Paint.Cap.ROUND
        passedArcPaint.isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val canvasSize = min(screenWidth, screenHeight) * 0.9F
        setMeasuredDimension(canvasSize.toInt(), canvasSize.toInt())

        center = canvasSize / 2F
        val size = canvasSize * 0.95F

        val radius = size / 2F
        teethR = radius * 0.985F
        numeralsR = radius * 0.85F
        hourHandR = radius * 0.5F
        minuteHandR = radius * 0.7F
        secondHandR = radius * 0.7F

        numeralsPaint.textSize = when (numeralsLanguage) {
            Language.ARABIC -> size * 0.08F
            Language.ENGLISH -> size * 0.065F
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        now.timeInMillis = System.currentTimeMillis()

        drawTeeth(canvas)
        drawNumerals(canvas)
        drawHands(canvas)
        if (timeFromPreviousPrayer != -1L) drawPassedArc(canvas)
        drawRemainingArc(canvas)

        postInvalidateDelayed(500)
    }

    private fun drawTeeth(canvas: Canvas) {
        for (i in 0..59) {
            val theta = i * 0.105F  // 0.105 is the distance between each two teeth spaces
            canvas.drawLine(
                center + teethR * cos(theta),
                center + teethR * sin(theta),
                center + (teethR + 10) * cos(theta),
                center + (teethR + 10) * sin(theta),
                teethPaint
            )
        }
    }

    private fun drawNumerals(canvas: Canvas) {
        for (number in numerals) {
            numeralsPaint.getTextBounds(number, 0, number.length, mRect)
            val angle = Math.PI / 6 * (number.toInt() - 3)
            val x = (center + cos(angle) * numeralsR - mRect.width() / 2).toInt()
            val y = (center + sin(angle) * numeralsR + mRect.height() / 2).toInt()
            canvas.drawText(number, x.toFloat(), y.toFloat(), numeralsPaint)
        }
    }

    private fun drawHands(canvas: Canvas) {
        val time = getTime()

        drawHourHand(canvas, (time[0] + time[1] / 60F) * 5)
        drawMinuteHand(canvas, time[1])
        drawSecondsHand(canvas, now.get(Calendar.SECOND).toFloat())
    }

    private fun drawHourHand(canvas: Canvas, location: Float) {
        val theta = getHandTheta(location)
        canvas.drawLine(
            (center - cos(theta) * 20).toFloat(),
            (center - sin(theta) * 20).toFloat(),
            (center + cos(theta) * hourHandR).toFloat(),
            (center + sin(theta) * hourHandR).toFloat(),
            hourHandPaint
        )
    }

    private fun drawMinuteHand(canvas: Canvas, location: Float) {
        val theta = getHandTheta(location)
        canvas.drawLine(
            (center - cos(theta) * 30).toFloat(),
            (center - sin(theta) * 30).toFloat(),
            (center + cos(theta) * minuteHandR).toFloat(),
            (center + sin(theta) * minuteHandR).toFloat(),
            minuteHandPaint
        )
    }

    private fun drawSecondsHand(canvas: Canvas, location: Float) {
        val theta = getHandTheta(location)
        canvas.drawLine(
            (center - cos(theta) * 40).toFloat(),
            (center - sin(theta) * 50).toFloat(),
            (center + cos(theta) * secondHandR).toFloat(),
            (center + sin(theta) * secondHandR).toFloat(),
            secondHandPaint
        )
    }

    private fun drawPassedArc(canvas: Canvas) {
        val strokeWidth = 10

        val start = strokeWidth / 2
        val end = 2 * center - start
        val rect = RectF(start.toFloat(), start.toFloat(), end, end)

        val fromTheta = timeFromPreviousPrayer / 1000 / 60 / 2F
        val thetaDiff = (System.currentTimeMillis() - timeFromPreviousPrayer) / 1000 / 60 / 2F
        canvas.drawArc(rect, fromTheta, thetaDiff, false, passedArcPaint)
    }

    private fun drawRemainingArc(canvas: Canvas) {
        val strokeWidth = 10

        val start = strokeWidth / 2
        val end = 2 * center - start
        val rect = RectF(start.toFloat(), start.toFloat(), end, end)

        val time = getTime()

        val nowTheta = -90 + 30 * (time[0] + time[1] / 60F)
        val thetaDiff = timeToNextPrayer / 1000 / 60 / 2F
        canvas.drawArc(rect, nowTheta, thetaDiff, false, secondHandPaint)
    }

    private fun getHandTheta(location: Float) = Math.PI * location / 30 - Math.PI / 2

    /**
     * It returns an array of two floats, the hour and the minute
     *
     * @return An array of floats.
     */
    private fun getTime(): Array<Float> {
        var hour = now.get(Calendar.HOUR_OF_DAY).toFloat()
        //convert to 12hour format from 24 hour format
        hour = if (hour > 12) hour - 12 else hour
        val minute = now.get(Calendar.MINUTE).toFloat()

        return arrayOf(hour, minute)
    }

    fun updateArcs(
        timeFromPreviousPrayer: Long,
        timeToNextPrayer: Long,
    ) {
        this.timeFromPreviousPrayer = timeFromPreviousPrayer
        this.timeToNextPrayer = timeToNextPrayer
    }

}