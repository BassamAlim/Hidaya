package bassamalim.hidaya.dialogs

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MyDialog
import bassamalim.hidaya.ui.components.MyImageButton
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PrefUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import java.util.*

class HijriDatePickerDialog(
    private val context: Context,
    private val shown: MutableState<Boolean>,
    private val selectionState: MutableState<UmmalquraCalendar>,
    private val onSet: () -> Unit
) {

    private val now = UmmalquraCalendar()
    private val selected = mutableStateListOf(
        selectionState.value[Calendar.YEAR], selectionState.value[Calendar.MONTH],
        selectionState.value[Calendar.DAY_OF_MONTH], selectionState.value[Calendar.DAY_OF_WEEK] - 1
    )
    private val minYear = now[Calendar.YEAR] - 100
    private val maxYear = now[Calendar.YEAR] + 100
    private val language = PrefUtils.getLanguage(context)
    private val months = context.resources.getStringArray(R.array.hijri_months)
    private val weekDays = context.resources.getStringArray(R.array.week_days)
    private val weekDaysAbb =
        if (language == "en") listOf("S", "M", "T", "W", "T", "F", "S")
        else listOf("أ", "إ", "ث", "أ", "خ", "ج", "س")
    private val divider = if (language == "en") "," else "،"

    private fun buildGrid(current: UmmalquraCalendar): Array<Array<String>> {
        val offset = current[Calendar.DAY_OF_WEEK] - 1
        val grid = Array(6) { row ->
            Array(7) { col ->
                val idx = row * 7 + col - offset
                if (row == 0 && col < offset || idx >= current.lengthOfMonth()) ""
                else (idx + 1).toString()
            }
        }
        return grid
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun MyHijriDatePickerDialog() {
        MyDialog(shown) {
            Column {
                // top area
                Box(
                    Modifier.background(AppTheme.colors.primary)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        // year
                        MyText(
                            text = LangUtils.translateNums(
                                context, selected[0].toString()
                            ),
                            fontSize = 18.sp
                        )

                        // main text
                        val mainText = "${weekDays[selected[3]]}$divider " +
                                "${LangUtils.translateNums(context, selected[2].toString())} " +
                                months[selected[1]]
                        MyText(
                            text = mainText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val pagerState = rememberPagerState(
                    initialPage = (selectionState.value[Calendar.YEAR] - minYear) * 12 +
                            selectionState.value[Calendar.MONTH]
                )

                val coroutineScope = rememberCoroutineScope()

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MyImageButton(R.drawable.ic_left_arrow) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }

                    // current month year
                    val absMonth = minYear * 12 + pagerState.currentPage
                    MyText(
                        "${months[absMonth % 12]} " +
                                LangUtils.translateNums(context, (absMonth / 12).toString()),
                        Modifier.width(150.dp)
                    )

                    MyImageButton(R.drawable.ic_right_arrow) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weekDaysAbb.forEach {
                        MyText(
                            it,
                            Modifier.size(40.dp),
                            fontSize = 16.sp
                        )
                    }
                }

                HorizontalPager(
                    count = (maxYear - minYear + 1) * 12,
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) { page ->
                    val absMonth = minYear * 12 + page
                    DaysGrid(absMonth)
                }

                // bottom area
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    // save
                    MyText(
                        stringResource(R.string.select),
                        Modifier
                            .padding(horizontal = 20.dp)
                            .clickable {
                                selectionState.value = UmmalquraCalendar(
                                    selected[0], selected[1], selected[2]
                                )
                                shown.value = false
                                onSet()
                            }
                    )

                    // cancel
                    MyText(
                        stringResource(R.string.cancel),
                        Modifier
                            .padding(horizontal = 20.dp)
                            .clickable {
                                shown.value = false
                            }
                    )
                }
            }
        }
    }

    @Composable
    private fun DaysGrid(absMonth: Int) {
        val current = UmmalquraCalendar()
        current[Calendar.YEAR] = absMonth / 12
        current[Calendar.MONTH] = absMonth % 12

        val grid = buildGrid(current)
        Box(
            Modifier.height(250.dp)
        ) {
            Column(
                Modifier.fillMaxWidth()
            ) {
                grid.forEachIndexed { _, ints ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ints.forEachIndexed { col, value ->
                            if (value.isEmpty()) MyText(value, Modifier.size(40.dp))
                            else {
                                MyText(
                                    LangUtils.translateNums(context, value),
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (current[Calendar.YEAR] == selected[0]
                                                && current[Calendar.MONTH] == selected[1]
                                                && value.toInt() == selected[2]
                                            ) AppTheme.colors.accent
                                            else AppTheme.colors.background
                                        )
                                        .clickable {
                                            selected[0] = current[Calendar.YEAR]
                                            selected[1] = current[Calendar.MONTH]
                                            selected[2] = value.toInt()
                                            selected[3] = col
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}