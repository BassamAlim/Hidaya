@file:OptIn(ExperimentalFoundationApi::class)

package bassamalim.hidaya.features.hijriDatePicker.ui

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
import androidx.compose.foundation.lazy.items
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

@Composable
fun HijriDatePickerDialog(
    viewModel: HijriDatePickerViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    if (state.isLoading) return

    val pagerState = rememberPagerState(
        initialPage = viewModel.initialPage,
        pageCount = viewModel.pageCount
    )

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(pagerState, coroutineScope)
        onDispose {}
    }

    MyDialog(shown = true) {
        Column(
            Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            TopArea(
                displayedYear = state.displayedYearText,
                mainText = state.mainText,
                onYearSelectorToggled = viewModel::onYearSelectorToggled
            )

            Box(Modifier.height(350.dp)) {
                when (state.selectorMode) {
                    SelectorMode.DAY_MONTH -> DayMonthSelector(
                        displayedMonth = state.displayedMonthText,
                        weekDaysAbb = state.weekDaysAbb,
                        pagerState = pagerState,
                        selectedDay = state.selectedDay,
                        currentDay = state.currentDay,
                        getDaysGrid = viewModel::getDaysGrid,
                        onPreviousMonthClick = viewModel::onPreviousMonthClick,
                        onNextMonthClick = viewModel::onNextMonthClick,
                        onDaySelected = viewModel::onDaySelected
                    )
                    SelectorMode.YEAR -> YearSelector(
                        selectedYear = state.displayedYearText,
                        yearOptions = state.yearSelectorItems,
                        onYearSelected = viewModel::onYearSelected
                    )
                }
            }

            BottomArea(
                onSelectClick = viewModel::onSelectClicked,
                onCancelClick = viewModel::onCancelClicked
            )
        }
    }
}

@Composable
private fun TopArea(
    displayedYear: String,
    mainText: String,
    onYearSelectorToggled: () -> Unit
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
                    .clickable { onYearSelectorToggled() }
            ) {
                MyText(
                    text = displayedYear,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    fontSize = 19.sp,
                    textColor = AppTheme.colors.onPrimary,
                )
            }

            // main text
            MyText(
                text = mainText,
                fontSize = 22.nsp,
                fontWeight = FontWeight.Bold,
                textColor = AppTheme.colors.onPrimary
            )
        }
    }
}

@Composable
private fun DayMonthSelector(
    displayedMonth: String,
    weekDaysAbb: List<String>,
    pagerState: PagerState,
    selectedDay: String,
    currentDay: String,
    getDaysGrid: (Int) -> List<List<String>>,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onDaySelected: (String) -> Unit
) {
    MyColumn {
        MonthSelector(
            displayedMonth = displayedMonth,
            onPreviousMonthClick = onPreviousMonthClick,
            onNextMonthClick = onNextMonthClick
        )

        DaySelector(
            weekDaysAbb = weekDaysAbb,
            pagerState = pagerState,
            getDaysGrid = getDaysGrid,
            selectedDay = selectedDay,
            currentDay = currentDay,
            onDaySelected = onDaySelected
        )
    }
}

@Composable
private fun MonthSelector(
    displayedMonth: String,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit
) {
    MyRow(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        MyIconButton(
            iconId = R.drawable.ic_left_arrow,
            size = 16.dp,
            tint = AppTheme.colors.onPrimary,
            onClick = onPreviousMonthClick
        )

        MyText(
            displayedMonth,
            Modifier.width(150.dp)
        )

        MyIconButton(
            iconId = R.drawable.ic_right_arrow,
            size = 16.dp,
            tint = AppTheme.colors.onPrimary,
            onClick = onNextMonthClick
        )
    }
}

@Composable
private fun DaySelector(
    weekDaysAbb: List<String>,
    pagerState: PagerState,
    getDaysGrid: (Int) -> List<List<String>>,
    selectedDay: String,
    currentDay: String,
    onDaySelected: (String) -> Unit
) {
    // week days
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

    // days grid
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) { page ->
        DaysGrid(
            daysGrid = getDaysGrid(page),
            selectedDay = selectedDay,
            currentDay = currentDay,
            onDaySelected = onDaySelected
        )
    }
}

@Composable
private fun BottomArea(
    onSelectClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // select
        MyClickableText(
            text = stringResource(R.string.select),
            modifier = Modifier.padding(start = 10.dp),
            onClick = onSelectClick
        )

        // cancel
        MyClickableText(
            text = stringResource(R.string.cancel),
            modifier = Modifier.padding(start = 10.dp),
            onClick = onCancelClick
        )
    }
}

@Composable
private fun DaysGrid(
    daysGrid: List<List<String>>,
    selectedDay: String,
    currentDay: String,
    onDaySelected: (String) -> Unit
) {
    Box(
        Modifier.height(250.dp)
    ) {
        Column(
            Modifier.fillMaxWidth()
        ) {
            daysGrid.forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { value ->
                        if (value.isEmpty()) MyText(value, Modifier.size(40.dp))
                        else {
                            MyText(
                                value,
                                Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (value == selectedDay) AppTheme.colors.accent
                                        else AppTheme.colors.background
                                    )
                                    .clickable { onDaySelected(value) },
                                textColor = when (value) {
                                    selectedDay -> AppTheme.colors.onPrimary
                                    currentDay -> AppTheme.colors.accent
                                    else -> AppTheme.colors.text
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
    selectedYear: String,
    yearOptions: List<String>,
    onYearSelected: (String) -> Unit
) {
    MyLazyColumn(
        state = rememberLazyListState(
            initialFirstVisibleItemIndex = yearOptions.indexOf(selectedYear) - 3,
            initialFirstVisibleItemScrollOffset = 0
        ),
        lazyList = {
            items(yearOptions) { item ->
                MyText(
                    item,
                    Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clickable { onYearSelected(item) },
                    textColor =
                        if (item == selectedYear) AppTheme.colors.accent
                        else AppTheme.colors.text
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp)
    )
}