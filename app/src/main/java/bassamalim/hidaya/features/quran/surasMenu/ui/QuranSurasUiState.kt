package bassamalim.hidaya.features.quran.surasMenu.ui

data class QuranSurasUiState(
    val isLoading: Boolean = true,
    val bookmarkPageText: String? = null,
    val bookmarkSuraText: String? = null,
    val searchText: String = "",
    val isTutorialDialogShown: Boolean = false
)
