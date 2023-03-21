package bassamalim.hidaya.features.quranSearcher

import bassamalim.hidaya.core.models.QuranSearcherMatch

data class QuranSearcherState(
    val matches: List<QuranSearcherMatch> = emptyList(),
    val noResultsFound: Boolean = false,
    val maxMatches: Int = 10
)
