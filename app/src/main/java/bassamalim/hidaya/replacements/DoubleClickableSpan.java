package bassamalim.bassamalim.hidaya.Replacements;

import android.text.style.ClickableSpan;
import android.view.View;

public abstract class DoubleClickableSpan extends ClickableSpan {
    public abstract void onDoubleClick(View view);
}
