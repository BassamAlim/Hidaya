package bassamalim.hidaya.features.main

data class MainState(
    val hijriDate: String = "",
    val gregorianDate: String = "",
    val dateEditorShown: Boolean = false,
    val dateOffset: Int = 0,
    val dateEditorOffsetText: String = "",
    val dateEditorDateText: String = ""
)