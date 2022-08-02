package bassamalim.hidaya.replacements

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Insets
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import androidx.annotation.NonNull
import bassamalim.hidaya.R
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class AnalogClockView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var mIsInit = false
    private var size = 900
    private var center = 0F
    private var teethR = 0F
    private var numeralsR = 0F
    private var hourHandR = 0F
    private var minuteHandR = 0F
    private var secondHandR = 0F
    private var mFontSize = 60F
    private var mRect = Rect()
    private val numeralsPaint = Paint()
    private val teethPaint = Paint()
    private val hourHandPaint = Paint()
    private val minuteHandPaint = Paint()
    private val secondHandPaint = Paint()
    private var mNumbers: IntArray = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    private var mHour = 0f
    private val bgColor = TypedValue()
    private val linesColor = TypedValue()
    private val accentColor = TypedValue()

    init {
        setupPaint()
    }

    private fun setupPaint() {
        context.theme.resolveAttribute(R.attr.myMainBg, bgColor, true)
        context.theme.resolveAttribute(R.attr.myText, linesColor, true)
        context.theme.resolveAttribute(R.attr.myAccent, accentColor, true)

        // Teeth
        teethPaint.style = Paint.Style.STROKE
        teethPaint.color = linesColor.data
        teethPaint.strokeWidth = 5F
        teethPaint.isAntiAlias = true

        // Numerals
        numeralsPaint.style = Paint.Style.FILL
        numeralsPaint.color = linesColor.data
        numeralsPaint.strokeWidth = 2F
        numeralsPaint.isAntiAlias = true
        numeralsPaint.textSize = mFontSize

        // Hour hand
        hourHandPaint.style = Paint.Style.STROKE
        hourHandPaint.color = linesColor.data
        hourHandPaint.strokeWidth = 11F
        hourHandPaint.isAntiAlias = true

        // Minute hand
        minuteHandPaint.style = Paint.Style.STROKE
        minuteHandPaint.color = linesColor.data
        minuteHandPaint.strokeWidth = 9F
        minuteHandPaint.isAntiAlias = true

        // Second hand
        secondHandPaint.style = Paint.Style.STROKE
        secondHandPaint.color = accentColor.data
        secondHandPaint.strokeWidth = 6F
        secondHandPaint.isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!mIsInit) init()

        drawTeeth(canvas!!)
        drawNumerals(canvas)
        drawHands(canvas)

        postInvalidateDelayed(500)
    }

    private fun getScreenWidth(@NonNull activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        }
        else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    private fun init() {
        val width = getScreenWidth(context as Activity)
        size = (width * 0.83).toInt()

        center = size / 2F

        val radius = size / 2F
        teethR = radius * 0.98F
        numeralsR = radius * 0.85F
        hourHandR = radius * 0.5F
        minuteHandR = radius * 0.7F
        secondHandR = radius * 0.7F

        mIsInit = true
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
        for (number in mNumbers) {
            val num = number.toString()
            numeralsPaint.getTextBounds(num, 0, num.length, mRect)
            val angle = Math.PI / 6 * (number - 3)
            val x = (center + cos(angle) * numeralsR - mRect.width() / 2).toInt()
            val y = (center + sin(angle) * numeralsR + mRect.height() / 2).toInt()
            canvas.drawText(num, x.toFloat(), y.toFloat(), numeralsPaint)
        }
    }

    private fun drawHands(canvas: Canvas) {
        val calendar: Calendar = Calendar.getInstance()
        mHour = calendar.get(Calendar.HOUR_OF_DAY).toFloat()
        //convert to 12hour format from 24 hour format
        mHour = if (mHour > 12) mHour - 12 else mHour
        val mMinute = calendar.get(Calendar.MINUTE).toFloat()

        drawHourHand(canvas, (mHour + mMinute / 60F) * 5)
        drawMinuteHand(canvas, mMinute)
        drawSecondsHand(canvas, calendar.get(Calendar.SECOND).toFloat())
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

    private fun getHandTheta(location: Float): Double {
        return Math.PI * location / 30 - Math.PI / 2
    }

}