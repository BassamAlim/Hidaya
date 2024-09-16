package bassamalim.hidaya.features.quran.surasMenu.ui

data class QuranSurasUiState(
    val isLoading: Boolean = true,
    val bookmarkPageText: String? = null,
    val bookmarkSuraText: String? = null,
    val searchText: String = "",
    val favs: Map<Int, Boolean> = emptyMap(),
    val isTutorialDialogShown: Boolean = false,
    val shouldShowPageDoesNotExist: Int = 0
)
