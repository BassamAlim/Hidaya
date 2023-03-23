package bassamalim.hidaya.features.quran

data class QuranState(
    val bookmarkedPageText: String = "",
    val favs: List<Int> = emptyList(),
    val searchText: String = "",
    val tutorialDialogShown: Boolean = false,
    val shouldShowPageDNE: Int = 0
)
