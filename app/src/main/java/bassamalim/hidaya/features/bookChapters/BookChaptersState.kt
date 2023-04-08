package bassamalim.hidaya.features.bookChapters

data class BookChaptersState(
    val title: String = "",
    val favs: List<Int> = emptyList(),
    val searchText: String = ""
)
