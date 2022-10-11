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
import androidx.compose.runtime.MutableState
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.utils.LangUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

class DateEditorDialog(private val dateOffset: MutableState<Int>) : DialogFragment() {

    private lateinit var pref: SharedPreferences
    private lateinit var dView: View
    private lateinit var calendar: UmmalquraCalendar
    private lateinit var dateTV: TextView
    private lateinit var offsetTV: TextView
    private var offset = 0

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

        offset = dateOffset.value

        getDate()

        setViews()

        updateTvs()

        setupListeners()

        return dView
    }

    private fun setViews() {
        dateTV = dView.findViewById(R.id.date_tv)
        offsetTV = dView.findViewById(R.id.offset_tv)
    }

    private fun getDate() {
        val cal = UmmalquraCalendar()

        val millisInDay = 1000 * 60 * 60 * 24
        cal.timeInMillis = cal.timeInMillis + offset * millisInDay

        calendar = cal
    }

    private fun updateTvs() {
        val text =
            "${calendar[Calendar.DATE]}/${calendar[Calendar.MONTH] + 1}/${calendar[Calendar.YEAR]}"
        dateTV.text = LangUtils.translateNums(requireContext(), text)

        if (offset == 0) offsetTV.text = getString(R.string.unchanged)
        else {
            var offsetStr = offset.toString()
            if (offset > 0) offsetStr = "+$offsetStr"
            offsetTV.text = LangUtils.translateNums(requireContext(), offsetStr)
        }
    }

    private fun setupListeners() {
        dView.findViewById<ImageButton>(R.id.day_back_btn).setOnClickListener {
            offset--
            getDate()
            updateTvs()
        }

        dView.findViewById<ImageButton>(R.id.day_forward_btn).setOnClickListener {
            offset++
            getDate()
            updateTvs()
        }

        dView.findViewById<Button>(R.id.cancel_btn).setOnClickListener {
            dialog!!.cancel()
        }

        dView.findViewById<Button>(R.id.save_btn).setOnClickListener {
            val editor = pref.edit()
            editor.putInt("date_offset", offset)
            editor.apply()

            dialog!!.dismiss()

            dateOffset.value = offset
        }
    }

}