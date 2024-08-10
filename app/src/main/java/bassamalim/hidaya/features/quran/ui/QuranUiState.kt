package bassamalim.hidaya.features.quran.ui

data class QuranUiState(
    val bookmarkPageText: String? = null,
    val bookmarkSuraText: String? = null,
    val searchText: String = "",
    val favs: Map<Int, Boolean> = emptyMap(),
    val isTutorialDialogShown: Boolean = false,
    val shouldShowPageDoesNotExist: Int = 0
)
