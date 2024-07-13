package bassamalim.hidaya.features.quranSearcher

data class QuranSearcherState(
    val matches: List<QuranSearcherMatch> = emptyList(),
    val noResultsFound: Boolean = false,
    val maxMatches: Int = 10
)
