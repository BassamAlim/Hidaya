package com.bassamalim.athkar;

import android.text.style.ClickableSpan;
import android.text.style.UpdateAppearance;
import android.view.View;

import androidx.annotation.NonNull;

public abstract class DoubleClickableSpan extends ClickableSpan implements UpdateAppearance {
    abstract public void onDoubleClick(View view);
}
