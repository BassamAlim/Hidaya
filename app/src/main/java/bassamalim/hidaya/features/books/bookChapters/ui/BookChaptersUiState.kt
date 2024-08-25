package bassamalim.hidaya.features.books.bookChapters.ui

data class BookChaptersUiState(
    val title: String = "",
    val favs: Map<Int, Boolean> = emptyMap(),
    val searchText: String = "",
)