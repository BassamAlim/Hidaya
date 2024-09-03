package bassamalim.hidaya.features.books.bookReader.ui

import bassamalim.hidaya.core.models.Book

data class BookReaderUiState(
    val bookTitle: String = "",
    val textSize: Float = 15f,
    val items: List<Book.BookChapter.BookDoor> = emptyList()
)
