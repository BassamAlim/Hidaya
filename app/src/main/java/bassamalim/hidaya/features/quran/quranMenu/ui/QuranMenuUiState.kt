package bassamalim.hidaya.features.quran.quranMenu.ui

data class QuranMenuUiState(
    val bookmarkPageText: String? = null,
    val bookmarkSuraText: String? = null,
    val searchText: String = "",
    val favs: Map<Int, Boolean> = emptyMap(),
    val isTutorialDialogShown: Boolean = false,
    val shouldShowPageDoesNotExist: Int = 0
)
