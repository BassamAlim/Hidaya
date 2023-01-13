package bassamalim.hidaya.state

data class DateConverterState(
    val hijriValues: List<String> = listOf("", "", ""),
    val gregorianValues: List<String> = listOf("", "", ""),
    val hijriDatePickerShown: Boolean = false
)