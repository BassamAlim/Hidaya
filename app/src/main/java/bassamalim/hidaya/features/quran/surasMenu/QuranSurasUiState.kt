package bassamalim.hidaya.features.quran.surasMenu

import bassamalim.hidaya.core.models.QuranBookmarks

data class QuranSurasUiState(
    val isLoading: Boolean = true,
    val isBookmarksExpanded: Boolean = false,
    val bookmarks: QuranBookmarks = QuranBookmarks(),
    val isTutorialActive: Boolean = false,
)
