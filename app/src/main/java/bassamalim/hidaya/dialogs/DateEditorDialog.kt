package bassamalim.hidaya.dialogs

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.utils.LangUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

class DateEditorDialog(private val refresher: Refresher?) : DialogFragment() {

    private lateinit var pref: SharedPreferences
    private lateinit var dView: View
    private lateinit var calendar: UmmalquraCalendar
    private var offset = 0

    interface Refresher {
        fun refresh()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        dView = inflater.inflate(R.layout.dialog_date_editor, container, false)

        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }

        offset = pref.getInt("date_offset", 0)

        getDate()

        updateTvs()

        setupListeners()

        return dView
    }

    private fun getDate() {
        val cal = UmmalquraCalendar()

        val millisInDay = 1000 * 60 * 60 * 24
        cal.timeInMillis = cal.timeInMillis + offset * millisInDay

        calendar = cal
    }

    private fun updateTvs() {
        val dateTv = dView.findViewById<TextView>(R.id.date_tv)
        val text =
            "${calendar[Calendar.DATE]}/${calendar[Calendar.MONTH] + 1}/${calendar[Calendar.YEAR]}"
        dateTv.text = LangUtils.translateNumbers(requireContext(), text)

        val offsetTv = dView.findViewById<TextView>(R.id.offset_tv)
        var offsetStr = offset.toString()
        if (offset > 0) offsetStr += "+"
        offsetTv.text = LangUtils.translateNumbers(requireContext(), offsetStr)
    }

    private fun setupListeners() {
        val prevBtn = dView.findViewById<ImageButton>(R.id.day_back_btn)
        prevBtn.setOnClickListener {
            offset--
            getDate()
            updateTvs()
        }

        val nextBtn = dView.findViewById<ImageButton>(R.id.day_forward_btn)
        nextBtn.setOnClickListener {
            offset++
            getDate()
            updateTvs()
        }

        val saveBtn = dView.findViewById<Button>(R.id.save_btn)
        saveBtn.setOnClickListener {
            val editor = pref.edit()
            editor.putInt("date_offset", offset)
            editor.apply()

            dialog!!.dismiss()

            refresher?.refresh()
        }
    }

}