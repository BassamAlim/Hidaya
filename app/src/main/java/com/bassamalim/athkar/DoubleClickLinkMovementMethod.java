package com.bassamalim.athkar;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class DoubleClickLinkMovementMethod extends LinkMovementMethod {

    //variable for storing the time of first click
    private long lastClick;
    //variable for storing the last clicked span
    private DoubleClickableSpan lastSpan;

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        Log.i(Constants.TAG, "in on touch event");

        int x = (int) event.getX();
        int y = (int) event.getY();
        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();
        x += widget.getScrollX();
        y += widget.getScrollY();
        Layout layout = widget.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);
        DoubleClickableSpan[] links = buffer.getSpans(off, off, DoubleClickableSpan.class);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (links.length != 0) {
                //constant for defining the time duration between the click that can be considered as double-tap
                int MAX_DURATION = 1200;
                if (System.currentTimeMillis() < lastClick + MAX_DURATION && links[0] == lastSpan) {
                    Log.i(Constants.TAG, "double clicked");
                    links[0].onDoubleClick(widget);
                    lastClick = 0;
                }
                else
                    lastClick = System.currentTimeMillis();
                lastSpan = links[0];
            }
        }
        return true;
    }

    private static DoubleClickLinkMovementMethod sInstance;
    public static DoubleClickLinkMovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new DoubleClickLinkMovementMethod();

        return sInstance;
    }

}
