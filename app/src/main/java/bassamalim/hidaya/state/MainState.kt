package bassamalim.hidaya.state

data class MainState(
    val hijriDate: String = "",
    val gregorianDate: String = "",
    val dateEditorShown: Boolean = false,
    val dateEditorOffsetText: String = "",
    val dateEditorDateText: String = ""
)