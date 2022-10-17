package bassamalim.hidaya.dialogs

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
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R

class TutorialDialog : DialogFragment() {

    private lateinit var pref: SharedPreferences
    private lateinit var text: String
    private lateinit var prefKey: String

    companion object {
        var TAG = "TutorialDialog"

        fun newInstance(text: String, prefKey: String): TutorialDialog {
            val dialog = TutorialDialog()
            val args = Bundle()
            args.putString("text", text)
            args.putString("pref_key", prefKey)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text = arguments?.getString("text", "")!!
        prefKey = arguments?.getString("pref_key", "")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.dialog_tutorial, container, false)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }

        val textTv = view.findViewById<TextView>(R.id.text_tv)
        textTv.text = text

        view.findViewById<ImageButton>(R.id.close_btn).setOnClickListener { dialog?.cancel() }

        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val doNotShowAgainCheckbox: CheckBox = requireView().findViewById(R.id.do_not_show_again_cb)
        if (doNotShowAgainCheckbox.isChecked) {
            pref.edit()
                .putBoolean(prefKey, false)
                .apply()
        }
    }

}