package com.bassamalim.athkar.other;

import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.View;

public abstract class DoubleClickableSpan extends ClickableSpan {
    public abstract void onDoubleClick(View view);
    public abstract void onClick(Spannable buffer, Object what);
}
