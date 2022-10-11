package bassamalim.hidaya.models

import androidx.compose.ui.text.AnnotatedString

data class BookSearcherMatch(
    val bookId: Int,
    val bookTitle: String,
    val chapterId: Int,
    val chapterTitle: String,
    val doorId: Int,
    val doorTitle: String,
    val text: AnnotatedString
)