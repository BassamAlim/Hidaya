package bassamalim.hidaya.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import bassamalim.hidaya.R
import bassamalim.hidaya.utils.LangUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

class HijriDatePickerDialog(private val listener: DatePickerDialog.OnDateSetListener) :
    DialogFragment() {

    private lateinit var dialogView: View
    private lateinit var yearPicker: NumberPicker
    private lateinit var monthPicker: NumberPicker
    private lateinit var dayPicker: NumberPicker
    private val hCalendar = UmmalquraCalendar()

    override fun onStart() {
        super.onStart()

        val dialog = dialog as AlertDialog

        val bgColor = TypedValue()
        requireContext().theme.resolveAttribute(R.attr.myMainBg, bgColor, false)
        dialog.window!!.setBackgroundDrawableResource(bgColor.data)

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            resources.getColor(R.color.text_M, requireContext().theme)
        )
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
            resources.getColor(R.color.text_M, requireContext().theme)
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = requireActivity().layoutInflater.inflate(R.layout.date_picker, null)

        dayPicker = setupDayPicker()
        monthPicker = setupMonthPicker()
        yearPicker = setupYearPicker()

        return AlertDialog.Builder(requireActivity()).setView(dialogView)
            .setPositiveButton(R.string.select) { _: DialogInterface?, _: Int ->
                val year = yearPicker.value
                listener.onDateSet(null, year, monthPicker.value, dayPicker.value)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .create()
    }

    private fun setupDayPicker(): NumberPicker {
        val dayPicker: NumberPicker = dialogView.findViewById(R.id.picker_day)

        val daysNums = arrayOfNulls<String>(30)
        for (i in daysNums.indices)
            daysNums[i] = LangUtils.translateNums(
                requireContext(), (i + 1).toString(), false
            )

        dayPicker.minValue = 1
        dayPicker.maxValue = hCalendar.lengthOfMonth()
        dayPicker.value = hCalendar[Calendar.DATE]
        dayPicker.displayedValues = daysNums
        dayPicker.setOnValueChangedListener { _: NumberPicker?, _: Int, newVal: Int ->
            hCalendar[Calendar.DATE] = newVal
        }

        return dayPicker
    }

    private fun setupMonthPicker(): NumberPicker {
        val monthPicker: NumberPicker = dialogView.findViewById(R.id.picker_month)

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = hCalendar[Calendar.MONTH] + 1
        monthPicker.displayedValues = resources.getStringArray(R.array.numbered_hijri_months)
        monthPicker.setOnValueChangedListener { _: NumberPicker?, _: Int, newVal: Int ->
            try {
                hCalendar[Calendar.MONTH] = newVal - 1
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.incorrect_day), Toast.LENGTH_SHORT).show()
            }

            dayPicker.maxValue = hCalendar.lengthOfMonth()
        }

        return monthPicker
    }

    private fun setupYearPicker(): NumberPicker {
        val yearPicker: NumberPicker = dialogView.findViewById(R.id.picker_year)

        val minYear = 1000
        val maxYear = 2000
        val tempArray = arrayOfNulls<String>(maxYear - minYear)
        for (i in minYear until maxYear)
            tempArray[i - minYear] = LangUtils.translateNums(
                requireContext(), i.toString(), false
            )

        yearPicker.minValue = minYear
        yearPicker.maxValue = maxYear - 1
        yearPicker.displayedValues = tempArray
        yearPicker.value = hCalendar[Calendar.YEAR] - minYear
        yearPicker.setOnValueChangedListener { _: NumberPicker?, _: Int, newVal: Int ->
            hCalendar[Calendar.YEAR] = newVal
            dayPicker.maxValue = hCalendar.lengthOfMonth()
        }

        return yearPicker
    }

}