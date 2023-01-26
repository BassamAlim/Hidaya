package bassamalim.hidaya.state

import bassamalim.hidaya.models.QuranSearcherMatch

data class QuranSearcherState(
    val matches: List<QuranSearcherMatch> = emptyList(),
    val noResultsFound: Boolean = false,
    val maxMatches: Int = 10
)
