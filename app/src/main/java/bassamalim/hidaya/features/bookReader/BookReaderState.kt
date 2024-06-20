package bassamalim.hidaya.features.bookReader

import bassamalim.hidaya.core.models.Book

data class BookReaderState(
    val bookTitle: String = "",
    val textSize: Float = 15f,
    val items: List<Book.BookChapter.BookDoor> = emptyList()
)
