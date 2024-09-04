package bassamalim.hidaya.features.quran.quranSuras.ui

data class QuranSurasUiState(
    val bookmarkPageText: String? = null,
    val bookmarkSuraText: String? = null,
    val searchText: String = "",
    val favs: Map<Int, Boolean> = emptyMap(),
    val isTutorialDialogShown: Boolean = false,
    val shouldShowPageDoesNotExist: Int = 0
)
