package bassamalim.hidaya.state

import bassamalim.hidaya.models.Sura

data class QuranState(
    val bookmarkedPageText: String = "",
    val items: List<Sura> = emptyList(),
    val favs: List<Int> = emptyList(),
    val isTutorialDialogShown: Boolean = false,
    val shouldShowPageDNE: Int = 0
)
