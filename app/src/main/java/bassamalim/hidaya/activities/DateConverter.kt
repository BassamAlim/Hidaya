package bassamalim.hidaya.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.dialogs.MyHijriDatePicker
//import bassamalim.hidaya.hijridatepicker.date.hijri.HijriDatePickerDialog
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.LangUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

class DateConverter : AppCompatActivity()/*, HijriDatePickerDialog.OnDateSetListener*/ {

    private val hijriValues = mutableStateListOf("", "", "")
    private val gregorianValues = mutableStateListOf("", "", "")
    private val hDatePickerShown = mutableStateOf(false)
    private val pickedHijri = mutableStateOf(UmmalquraCalendar())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)

        setContent {
            AppTheme {
                UI()
            }
        }
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
        hDatePickerShown.value = true

        /*val now = UmmalquraCalendar()
        val dpd = HijriDatePickerDialog.newInstance(
            this,
            now.get(UmmalquraCalendar.YEAR),
            now.get(UmmalquraCalendar.MONTH),
            now.get(UmmalquraCalendar.DAY_OF_MONTH)
        )
        dpd.show(supportFragmentManager, "HijriDatePickerDialog")*/

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
        hijriValues[0] = LangUtils.translateNums(
            this, hijri[Calendar.YEAR].toString(), false
        )
        hijriValues[1] = resources.getStringArray(
            R.array.numbered_hijri_months
        )[hijri[Calendar.MONTH]]
        hijriValues[2] = LangUtils.translateNums(
            this, hijri[Calendar.DATE].toString(), false
        )

        gregorianValues[0] = LangUtils.translateNums(
            this, gregorian[Calendar.YEAR].toString(), false
        )
        gregorianValues[1] = resources.getStringArray(
            R.array.numbered_gregorian_months
        )[gregorian[Calendar.MONTH]]
        gregorianValues[2] = LangUtils.translateNums(
            this, gregorian[Calendar.DATE].toString(), false
        )
    }

    /*override fun onDateSet(
        view: HijriDatePickerDialog?,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ) {
        //YOUR CODE
        val choice = UmmalquraCalendar(year, monthOfYear - 1, dayOfMonth)
        show(choice, hijriToGregorian(choice))
    }*/

    @Composable
    private fun UI() {
        MyScaffold(stringResource(id = R.string.date_converter)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 50.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column {
                    MyButton(
                        text = stringResource(id = R.string.pick_hijri_date),
                        fontSize = 22.sp,
                        modifier = Modifier.padding(vertical = 15.dp),
                        innerPadding = PaddingValues(vertical = 10.dp, horizontal = 15.dp)
                    ) {
                        pickHijri()
                    }

                    MyButton(
                        text = stringResource(id = R.string.pick_gregorian_date),
                        fontSize = 22.sp,
                        modifier = Modifier.padding(vertical = 15.dp),
                        innerPadding = PaddingValues(vertical = 10.dp, horizontal = 15.dp)
                    ) {
                        pickGregorian()
                    }
                }

                ResultSpace(stringResource(id = R.string.hijri_date), hijriValues)

                ResultSpace(stringResource(id = R.string.gregorian_date), gregorianValues)
            }

            if (hDatePickerShown.value) {
                MyHijriDatePicker(
                    this, hDatePickerShown, pickedHijri
                ).MyHijriDatePickerDialog()
            }
        }
    }

    @Composable
    private fun ResultSpace(title: String, values: SnapshotStateList<String>) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(
                    width = 3.dp,
                    color = AppTheme.colors.accent,
                    shape = RoundedCornerShape(size = 14.dp)
                ),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = title,
                modifier = Modifier.padding(10.dp),
                fontSize = 22.sp
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MyText(
                        text = stringResource(id = R.string.day),
                        Modifier.padding(10.dp)
                    )

                    MyText(
                        text = values[2],
                        Modifier.padding(10.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MyText(
                        text = stringResource(id = R.string.month),
                        Modifier.padding(10.dp)
                    )

                    MyText(
                        text = values[1],
                        Modifier.padding(10.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MyText(
                        text = stringResource(id = R.string.year),
                        Modifier.padding(10.dp)
                    )

                    MyText(
                        text = values[0],
                        Modifier.padding(10.dp)
                    )
                }
            }
        }
    }

}