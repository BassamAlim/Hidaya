package bassamalim.hidaya.features.quran.quranSearcher.ui

data class QuranSearcherUiState(
    val searchText: String = "",
    val matches: List<QuranSearcherMatch> = emptyList(),
    val isNoResultsFound: Boolean = false,
    val maxMatches: Int = 10
)
