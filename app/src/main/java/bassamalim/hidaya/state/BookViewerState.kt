package bassamalim.hidaya.state

import bassamalim.hidaya.models.Book

data class BookViewerState(
    val textSize: Float = 15f,
    val items: List<Book.BookChapter.BookDoor> = emptyList()
)
