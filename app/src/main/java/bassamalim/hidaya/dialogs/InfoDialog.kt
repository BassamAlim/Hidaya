package bassamalim.hidaya.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import bassamalim.hidaya.R

class InfoDialog(private val title: String, private val text: String) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.dialog_info, container, false)
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }
        val titleTv = view.findViewById<TextView>(R.id.title_tv)
        titleTv.text = title
        val textTv = view.findViewById<TextView>(R.id.text_tv)
        textTv.text = text
        return view
    }

    companion object {
        var TAG = "InfoDialog"
    }
}