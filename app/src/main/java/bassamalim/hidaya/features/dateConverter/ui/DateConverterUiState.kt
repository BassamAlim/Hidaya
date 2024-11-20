package bassamalim.hidaya.features.dateConverter.ui

data class DateConverterUiState(
    val hijriDate: Date = Date("", "", ""),
    val gregorianDate: Date = Date("", "", ""),
    val isGregorianDatePickerShown: Boolean = false,
    val gregorianDatePickerMillis: Long = 0L
)