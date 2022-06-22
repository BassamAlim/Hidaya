package bassamalim.hidaya.dialogs

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R

class TutorialDialog : DialogFragment {

    private lateinit var pref: SharedPreferences
    private lateinit var gView: View
    private lateinit var text: String
    private lateinit var prefKey: String

    companion object {
        var TAG = "TutorialDialog"
    }

    constructor()

    constructor(context: Context, text: String, prefKey: String) {
        this.text = text
        this.prefKey = prefKey

        pref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        gView = inflater.inflate(R.layout.dialog_tutorial, container, false)

        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }

        val textTv: TextView = gView.findViewById(R.id.text_tv)
        textTv.text = text

        return gView
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val doNotShowAgainCheckbox: CheckBox = requireView().findViewById(R.id.do_not_show_again_cb)
        if (doNotShowAgainCheckbox.isChecked) {
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putBoolean(prefKey, false)
            editor.apply()
        }
    }

}