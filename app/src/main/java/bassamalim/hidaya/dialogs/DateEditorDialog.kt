package bassamalim.hidaya.dialogs

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyDialog
import bassamalim.hidaya.ui.components.MyImageButton
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.LangUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.*

class DateEditorDialog(
    private val context: Context,
    private val pref: SharedPreferences,
    private val dateOffset: MutableState<Int>,
    private val shown: MutableState<Boolean>
) {

    private lateinit var calendar: UmmalquraCalendar
    private var offset = 0
    private val dateText = mutableStateOf("")
    private val offsetText = mutableStateOf("")

    init {
        offset = dateOffset.value

        getDate()

        updateTvs()
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
        dateText.value =LangUtils.translateNums(context, text)

        if (offset == 0) offsetText.value = context.getString(R.string.unchanged)
        else {
            var offsetStr = offset.toString()
            if (offset > 0) offsetStr = "+$offsetStr"
            offsetText.value = LangUtils.translateNums(context, offsetStr)
        }
    }

    @Composable
    fun Dialog() {
        MyDialog(shown) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(
                    stringResource(R.string.adjust_date),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                MyText(
                    offsetText.value,
                    textColor = AppTheme.colors.accent,
                    fontSize = 22.sp
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MyImageButton(
                        imageResId = R.drawable.ic_left_arrow
                    ) {
                        offset--
                        getDate()
                        updateTvs()
                    }

                    MyText(dateText.value, fontSize = 22.sp)

                    MyImageButton(
                        imageResId = R.drawable.ic_right_arrow
                    ) {
                        offset++
                        getDate()
                        updateTvs()
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MyButton(
                        text = stringResource(R.string.save)
                    ) {
                        pref.edit()
                            .putInt("date_offset", offset)
                            .apply()

                        shown.value = false

                        dateOffset.value = offset
                    }

                    MyButton(
                        text = stringResource(R.string.cancel)
                    ) {
                        shown.value = false
                    }
                }
            }
        }
    }

}