package bassamalim.hidaya.features.hijriDatePicker.ui

data class HijriDatePickerUiState(
    val isLoading: Boolean = true,
    val selectorMode: SelectorMode = SelectorMode.DAY_MONTH,
    val yearSelectorItems: List<String> = emptyList(),
    val displayedYearText: String = "",
    val mainText: String = "",
    val displayedMonthText: String = "",
    val weekDaysAbb: List<String> = emptyList(),
    val isSelectedDayDisplayed: Boolean = true,
    val selectedDay: String = "-"
)