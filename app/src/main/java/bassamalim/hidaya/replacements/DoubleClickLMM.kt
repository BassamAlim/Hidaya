package bassamalim.hidaya.replacements

import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.widget.TextView

class DoubleClickLMM : LinkMovementMethod() {

    //variable for storing the time of first click
    private var lastClick: Long = 0

    //variable for storing the last clicked span
    private var lastSpan: DoubleClickableSpan? = null

    //variable for storing the last clicked string
    private var lastBuffer: Spannable? = null

    // stores to set span if no span is set even if its the last span
    private var firstClick = false

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val arr = getSpan(widget, buffer, event)
            if (arr.isNotEmpty()) {
                val pressedSpan = arr[0]

                var same = false
                if (lastSpan == null || firstClick) {
                    setSpan(buffer, pressedSpan)
                    firstClick = false
                }
                else {
                    if (pressedSpan === lastSpan) {
                        same = true
                        firstClick = true
                    }
                    lastBuffer!!.removeSpan(what)
                    if (!same) setSpan(buffer, pressedSpan)
                }

                //constant for defining the time duration between the click that can be
                // considered as double-tap
                val maxDuration = 1200
                lastClick =
                    if (System.currentTimeMillis() < lastClick + maxDuration
                        && pressedSpan === lastSpan) {
                        pressedSpan.onDoubleClick(widget)
                        0
                    }
                    else {
                        pressedSpan.onClick(widget)
                        System.currentTimeMillis()
                    }
                lastSpan = pressedSpan
                lastBuffer = buffer
            }
        }
        return true
    }

    private fun setSpan(buffer: Spannable, pressedSpan: DoubleClickableSpan) {
        buffer.setSpan(
            what, buffer.getSpanStart(pressedSpan),
            buffer.getSpanEnd(pressedSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun getSpan(
        widget: TextView, buffer: Spannable, event: MotionEvent
    ): Array<DoubleClickableSpan> {
        var x = event.x.toInt()
        var y = event.y.toInt()
        x -= widget.totalPaddingLeft
        y -= widget.totalPaddingTop
        x += widget.scrollX
        y += widget.scrollY
        val layout = widget.layout
        val line = layout.getLineForVertical(y)
        val off = layout.getOffsetForHorizontal(line, x.toFloat())
        return buffer.getSpans(off, off, DoubleClickableSpan::class.java)
    }

    companion object {
        // the spanning configuration
        private var what: Any? = null
        private var sInstance: DoubleClickLMM? = null
        fun getInstance(color: Int): DoubleClickLMM {
            // background span has a problem (covers part of the text)
            //what = new BackgroundColorSpan(Color.Black);

            what = ForegroundColorSpan(color)
            if (sInstance == null) sInstance = DoubleClickLMM()
            return sInstance as DoubleClickLMM
        }
    }
}