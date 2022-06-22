package bassamalim.hidaya.replacements

import android.text.style.ClickableSpan
import android.view.View

abstract class DoubleClickableSpan : ClickableSpan() {

    abstract fun onDoubleClick(view: View?)

}