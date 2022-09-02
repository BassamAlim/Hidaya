package bassamalim.hidaya.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import bassamalim.hidaya.R

class InfoDialog: DialogFragment() {

    private lateinit var title: String
    private lateinit var text: String

    companion object {
        var TAG = "InfoDialog"

        fun newInstance(title: String, text: String): InfoDialog {
            val dialog = InfoDialog()
            val args = Bundle()
            args.putString("title", title)
            args.putString("text", text)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = arguments?.getString("title", "")!!
        text = arguments?.getString("text", "")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.dialog_info, container, false)

        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }

        val titleTv = view.findViewById<TextView>(R.id.title_tv)
        titleTv.text = title

        val textTv = view.findViewById<TextView>(R.id.text_tv)
        textTv.text = text

        view.findViewById<ImageButton>(R.id.close_btn).setOnClickListener { dialog?.cancel() }

        return view
    }

}