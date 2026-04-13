package bassamalim.hidaya.features.books.bookReader

import bassamalim.hidaya.core.models.BookContent

data class BookReaderUiState(
    val isLoading: Boolean = false,
    val bookTitle: String = "",
    val doors: List<BookContent.Chapter.Door> = emptyList(),
    val textSize: Float = 15f
)
