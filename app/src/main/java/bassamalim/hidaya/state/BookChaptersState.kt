package bassamalim.hidaya.state

import bassamalim.hidaya.models.BookChapter

data class BookChaptersState(
    val title: String = "",
    val items: List<BookChapter> = emptyList(),
    val favs: List<Int> = emptyList()
)
