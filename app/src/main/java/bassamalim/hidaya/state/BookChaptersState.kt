package bassamalim.hidaya.state

import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.BookChapter

data class BookChaptersState(
    val title: String = "",
    val listType: ListType = ListType.All,
    val items: List<BookChapter> = emptyList(),
    val favs: List<Int> = emptyList()
)
