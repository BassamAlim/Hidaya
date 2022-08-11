package bassamalim.hidaya.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityDateConverterBinding
import bassamalim.hidaya.dialogs.HijriDatePickerDialog
import bassamalim.hidaya.other.Utils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

class DateConverter : AppCompatActivity() {

    private lateinit var binding: ActivityDateConverterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityDateConverterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.home.setOnClickListener { onBackPressed() }

        setListeners()
    }

    private fun setListeners() {
        binding.hijriToGregorianBtn.setOnClickListener { pickHijri() }
        binding.gregorianToHijriBtn.setOnClickListener { pickGregorian() }
    }

    private fun pickGregorian() {
        val now = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _: DatePicker?, year: Int, month: Int, day: Int ->
                val choice = Calendar.getInstance()
                choice[Calendar.YEAR] = year
                choice[Calendar.MONTH] = month // starts from 0
                choice[Calendar.DATE] = day

                show(gregorianToHijri(choice), choice)
            }, now[Calendar.YEAR], now[Calendar.MONTH], now[Calendar.DATE]
        )
        datePicker.setButton(
            DatePickerDialog.BUTTON_POSITIVE,
            getString(R.string.select), datePicker
        )
        datePicker.setButton(
            DatePickerDialog.BUTTON_NEGATIVE,
            getString(R.string.cancel), datePicker
        )
        datePicker.show()
    }

    private fun pickHijri() {
        HijriDatePickerDialog { _: DatePicker?, year: Int, month: Int, day: Int ->
            val choice: Calendar = UmmalquraCalendar(year, month - 1, day)
            show(choice, hijriToGregorian(choice))
        }.show(supportFragmentManager, "HijriDatePicker")
    }

    private fun gregorianToHijri(gregorian: Calendar): Calendar {
        val hijri = UmmalquraCalendar()
        hijri.time = gregorian.time
        return hijri
    }

    private fun hijriToGregorian(hijri: Calendar): Calendar {
        val gregorian = Calendar.getInstance()
        gregorian.time = hijri.time
        return gregorian
    }

    private fun show(hijri: Calendar, gregorian: Calendar) {
        binding.hijriYearTv.text =
            Utils.translateNumbers(this, hijri[Calendar.YEAR].toString(), false)
        binding.hijriMonthTv.text =
            resources.getStringArray(R.array.numbered_hijri_months)[hijri[Calendar.MONTH]]
        binding.hijriDayTv.text =
            Utils.translateNumbers(this, hijri[Calendar.DATE].toString(), false)

        binding.gregorianYearTv.text = Utils.translateNumbers(
            this, gregorian[Calendar.YEAR].toString(), false
        )
        binding.gregorianMonthTv.text =
            resources.getStringArray(R.array.numbered_gregorian_months)[gregorian[Calendar.MONTH]]
        binding.gregorianDayTv.text = Utils.translateNumbers(
            this, gregorian[Calendar.DATE].toString(), false
        )
    }

}