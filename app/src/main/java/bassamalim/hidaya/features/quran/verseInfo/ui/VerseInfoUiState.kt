package bassamalim.hidaya.features.quran.verseInfo.ui

import androidx.compose.ui.text.AnnotatedString
import bassamalim.hidaya.core.models.QuranBookmarks

data class VerseInfoUiState(
    val isLoading: Boolean = true,
    val verseId: Int = 0,
    val verseText: String = "",
    val interpretation: AnnotatedString = AnnotatedString(""),
    val bookmarks: QuranBookmarks = QuranBookmarks()
)