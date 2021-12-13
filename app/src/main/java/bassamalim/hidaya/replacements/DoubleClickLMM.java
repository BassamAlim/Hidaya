package bassamalim.bassamalim.hidaya.Replacements;

import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.view.MotionEvent;
import android.widget.TextView;

public class DoubleClickLMM extends LinkMovementMethod {

    //variable for storing the time of first click
    private long lastClick;
    //variable for storing the last clicked span
    private DoubleClickableSpan lastSpan;
    //variable for storing the last clicked string
    private Spannable lastBuffer;
    // the spanning configuration
    private static Object what;
    // stores to set span if no span is set even if its the last span
    private boolean firstClick = false;

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            DoubleClickableSpan[] arr = getSpan(widget, buffer, event);
            if (arr.length > 0) {
                DoubleClickableSpan pressedSpan = arr[0];

                boolean same = false;
                if (lastSpan == null || firstClick) {
                    setSpan(buffer, pressedSpan);
                    firstClick = false;
                }
                else {
                    if (pressedSpan == lastSpan) {
                        same = true;
                        firstClick = true;
                    }
                    lastBuffer.removeSpan(what);
                    if (!same)
                        setSpan(buffer, pressedSpan);
                }

                //constant for defining the time duration between the click that can be
                // considered as double-tap
                int MAX_DURATION = 1200;
                if (System.currentTimeMillis() < lastClick + MAX_DURATION &&
                        pressedSpan == lastSpan) {
                    pressedSpan.onDoubleClick(widget);
                    lastClick = 0;
                }
                else {
                    pressedSpan.onClick(widget);
                    lastClick = System.currentTimeMillis();
                }
                lastSpan = pressedSpan;
                lastBuffer = buffer;
            }
        }
        return true;
    }

    private void setSpan(Spannable buffer, DoubleClickableSpan pressedSpan) {
        buffer.setSpan(what, buffer.getSpanStart(
                pressedSpan), buffer.getSpanEnd(pressedSpan),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private DoubleClickableSpan[] getSpan(TextView widget, Spannable buffer, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();
        x += widget.getScrollX();
        y += widget.getScrollY();
        Layout layout = widget.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);
        return buffer.getSpans(off, off, DoubleClickableSpan.class);
    }

    private static DoubleClickLMM sInstance;
    public static DoubleClickLMM getInstance(int color) {
        what = new BackgroundColorSpan(color);
        if (sInstance == null)
            sInstance = new DoubleClickLMM();

        return sInstance;
    }
}
