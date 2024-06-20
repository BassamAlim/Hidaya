@file:OptIn(ExperimentalFoundationApi::class)

package bassamalim.hidaya.features.hijriDatePicker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyClickableText
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyDialog
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.util.Calendar

@Composable
fun HijriDatePickerDialog(
    vm: HijriDatePickerViewModel
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = (st.selected[Calendar.YEAR] - vm.minYear) * 12
                + st.selected[Calendar.MONTH],
        pageCount = { (vm.maxYear - vm.minYear + 1) * 12 }
    )
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(key1 = vm) {
        vm.onStart(pagerState, coroutineScope)
        onDispose {}
    }

    MyDialog(shown = true) {
        Column(
            Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            TopArea(vm, st, pagerState)

            Box(Modifier.height(350.dp)) {
                when (st.selectorMode) {
                    SelectorMode.DAY_MONTH -> DayMonthSelector(vm, st, pagerState)
                    SelectorMode.YEAR -> YearSelector(vm)
                }
            }

            BottomArea(vm)
        }
    }
}

@Composable
private fun TopArea(
    vm: HijriDatePickerViewModel,
    st: HijriDatePickerState,
    pagerState: PagerState
) {
    Box(
        Modifier.background(AppTheme.colors.primary)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 20.dp)
        ) {
            // year
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { vm.onYearSelectorToggled() }
            ) {
                MyText(
                    text = translateNums(
                        vm.numeralsLanguage,
                        ((vm.minYear * 12 + pagerState.currentPage) / 12).toString()
                    ),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    fontSize = 19.sp,
                    textColor = AppTheme.colors.onPrimary,
                )
            }

            // main text
            MyText(
                text = "${vm.weekDays[st.selected[Calendar.DAY_OF_WEEK]-1]} " +
                        "${translateNums(vm.numeralsLanguage, st.selected[Calendar.DATE].toString())} " +
                        vm.months[st.selected[Calendar.MONTH]],
                fontSize = 22.nsp,
                fontWeight = FontWeight.Bold,
                textColor = AppTheme.colors.onPrimary
            )
        }
    }
}

@Composable
private fun DayMonthSelector(
    vm: HijriDatePickerViewModel,
    st: HijriDatePickerState,
    pagerState: PagerState
) {
    MyColumn {
        // month selector
        MyRow(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            MyIconButton(
                iconId = R.drawable.ic_left_arrow,
                size = 16.dp,
                tint = AppTheme.colors.onPrimary,
                onClick = { vm.onPrevMonthClk() }
            )

            // current month year
            val absMonth = vm.minYear * 12 + pagerState.currentPage
            MyText(
                "${vm.months[absMonth % 12]} " +
                        translateNums(vm.numeralsLanguage, (absMonth / 12).toString()),
                Modifier.width(150.dp)
            )

            MyIconButton(
                iconId = R.drawable.ic_right_arrow,
                size = 16.dp,
                tint = AppTheme.colors.onPrimary,
                onClick = { vm.onNextMonthClk() }
            )
        }

        // week days
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            vm.weekDaysAbb.forEach {
                MyText(
                    it,
                    Modifier.size(40.dp),
                    fontSize = 16.sp
                )
            }
        }

        // days grid
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) { page ->
            DaysGrid(
                vm, st,
                absMonth = vm.minYear * 12 + page
            )
        }
    }
}

@Composable
private fun BottomArea(
    vm: HijriDatePickerViewModel
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // select
        MyClickableText(
            stringResource(R.string.select),
            Modifier.padding(start = 10.dp)
        ) {
            vm.onDateSelected()
        }

        // cancel
        MyClickableText(
            stringResource(R.string.cancel),
            Modifier.padding(start = 10.dp)
        ) {
            vm.onCanceled()
        }
    }
}

@Composable
private fun DaysGrid(
    vm: HijriDatePickerViewModel,
    st: HijriDatePickerState,
    absMonth: Int
) {
    val current = UmmalquraCalendar()
    current[Calendar.YEAR] = absMonth / 12
    current[Calendar.MONTH] = absMonth % 12

    val rows = vm.buildGrid(current)
    Box(
        Modifier.height(250.dp)
    ) {
        Column(
            Modifier.fillMaxWidth()
        ) {
            rows.forEachIndexed { _, row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEachIndexed { _, value ->
                        if (value.isEmpty()) MyText(value, Modifier.size(40.dp))
                        else {
                            val isSelected = current[Calendar.YEAR] == st.selected[Calendar.YEAR]
                                    && current[Calendar.MONTH] == st.selected[Calendar.MONTH]
                                    && value.toInt() == st.selected[Calendar.DATE]
                            val isToday = current[Calendar.YEAR] == vm.now[Calendar.YEAR]
                                    && current[Calendar.MONTH] == vm.now[Calendar.MONTH]
                                    && value.toInt() == vm.now[Calendar.DATE]

                            MyText(
                                translateNums(vm.numeralsLanguage, value),
                                textColor =
                                if (isSelected) AppTheme.colors.onPrimary
                                else if (isToday) AppTheme.colors.accent
                                else AppTheme.colors.text,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) AppTheme.colors.accent
                                        else AppTheme.colors.background
                                    )
                                    .clickable {
                                        vm.onDaySelected(
                                            current[Calendar.YEAR],
                                            current[Calendar.MONTH],
                                            value.toInt()
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearSelector(
    vm: HijriDatePickerViewModel
) {
    val lazyListState = rememberLazyListState(
        vm.now[Calendar.YEAR] - vm.minYear - 3,
        0
    )

    MyLazyColumn(
        state = lazyListState,
        lazyList = {
            items(vm.maxYear - vm.minYear + 1) { idx ->
                val year = vm.minYear + idx
                MyText(
                    text = translateNums(vm.numeralsLanguage, year.toString()),
                    textColor =
                    if (year == vm.now[Calendar.YEAR]) AppTheme.colors.accent
                    else AppTheme.colors.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clickable { vm.onYearSelected(year) }
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp)
    )
}