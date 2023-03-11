package bassamalim.hidaya.features.dateConverter

data class DateConverterState(
    val hijriValues: List<String> = listOf("", "", ""),
    val gregorianValues: List<String> = listOf("", "", ""),
    val hijriDatePickerShown: Boolean = false
)