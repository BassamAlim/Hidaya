package bassamalim.hidaya.features.quran

import bassamalim.hidaya.core.models.Sura

data class QuranState(
    val bookmarkedPageText: String = "",
    val items: List<Sura> = emptyList(),
    val favs: List<Int> = emptyList(),
    val tutorialDialogShown: Boolean = false,
    val shouldShowPageDNE: Int = 0
)
