package bassamalim.hidaya.features.bookChapters

import bassamalim.hidaya.core.models.BookChapter

data class BookChaptersState(
    val title: String = "",
    val items: List<BookChapter> = emptyList(),
    val favs: List<Int> = emptyList()
)
