package bassamalim.hidaya.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import bassamalim.hidaya.R
import bassamalim.hidaya.other.Utils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

class HijriDatePickerDialog : DialogFragment() {
    private var listener: DatePickerDialog.OnDateSetListener? = null
    private var monthPicker: NumberPicker? = null
    private var yearPicker: NumberPicker? = null
    private var dayPicker: NumberPicker? = null
    private val cal: UmmalquraCalendar = UmmalquraCalendar()
    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setBackgroundDrawableResource(R.color.bg_M)
        (dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            resources.getColor(R.color.text_M, requireContext().theme)
        )
        (dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
            resources.getColor(R.color.text_M, requireContext().theme)
        )
    }

    fun setListener(listener: DatePickerDialog.OnDateSetListener?) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: View = requireActivity().layoutInflater
            .inflate(R.layout.date_picker, null)
        yearPicker = dialog.findViewById(R.id.picker_year)
        monthPicker = dialog.findViewById(R.id.picker_month)
        dayPicker = dialog.findViewById(R.id.picker_day)
        val MAX_YEAR = 2000
        val tempArray = arrayOfNulls<String>(MAX_YEAR)
        for (i in 0 until MAX_YEAR) tempArray[i] = Utils.translateNumbers(
            requireContext(), (i + 1).toString()
        )
        yearPicker!!.minValue = 1
        yearPicker!!.maxValue = MAX_YEAR
        yearPicker!!.displayedValues = tempArray
        yearPicker!!.value = cal.get(Calendar.YEAR)
        yearPicker!!.setOnValueChangedListener { _: NumberPicker?, _: Int, newVal: Int ->
            cal.set(Calendar.YEAR, newVal)
            dayPicker!!.maxValue = cal.lengthOfMonth()
        }
        monthPicker!!.minValue = 1
        monthPicker!!.maxValue = 12
        monthPicker!!.value = cal.get(Calendar.MONTH) + 1
        monthPicker!!.displayedValues = resources
            .getStringArray(R.array.numbered_hijri_months)
        monthPicker!!.setOnValueChangedListener { _: NumberPicker?, _: Int, newVal: Int ->
            cal.set(Calendar.MONTH, newVal - 1)
            dayPicker!!.maxValue = cal.lengthOfMonth()
        }
        val daysNums = arrayOfNulls<String>(30)
        for (i in daysNums.indices) daysNums[i] = Utils.translateNumbers(
            requireContext(), (i + 1).toString()
        )
        dayPicker!!.minValue = 1
        dayPicker!!.maxValue = cal.lengthOfMonth()
        dayPicker!!.value = cal.get(Calendar.DATE)
        dayPicker!!.displayedValues = daysNums
        dayPicker!!.setOnValueChangedListener { _: NumberPicker?, _: Int, newVal: Int ->
            cal.set(
                Calendar.DATE,
                newVal
            )
        }
        return AlertDialog.Builder(requireActivity()).setView(dialog)
            .setPositiveButton(R.string.select) { _: DialogInterface?, _: Int ->
                val year: Int = yearPicker!!.value
                listener!!.onDateSet(
                    null, year,
                    monthPicker!!.value, dayPicker!!.value
                )
            }
            .setNegativeButton(R.string.cancel) { dialog12, _ ->
                dialog12.cancel()
            }.create()
    }
}