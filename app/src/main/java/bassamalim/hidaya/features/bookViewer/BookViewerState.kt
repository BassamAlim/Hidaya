package bassamalim.hidaya.features.bookViewer

import bassamalim.hidaya.core.models.Book

data class BookViewerState(
    val textSize: Float = 15f,
    val items: List<Book.BookChapter.BookDoor> = emptyList()
)
