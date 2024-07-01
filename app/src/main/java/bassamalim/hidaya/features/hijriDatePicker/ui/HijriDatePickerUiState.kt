package bassamalim.hidaya.features.hijriDatePicker.ui

data class HijriDatePickerUiState(
    val selectorMode: SelectorMode = SelectorMode.DAY_MONTH,
    val yearSelectorItems: List<String> = emptyList(),
    val displayedYear: String = "",
    val mainText: String = "",
    val displayedMonth: String = "",
    val selectedDay: String = ".",
    val currentDay: String = ".",
    val weekDaysAbb: List<String> = emptyList(),
)