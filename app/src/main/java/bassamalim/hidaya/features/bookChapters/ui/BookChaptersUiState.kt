package bassamalim.hidaya.features.bookChapters.ui

data class BookChaptersUiState(
    val title: String = "",
    val favs: Map<Int, Int> = emptyMap(),
    val searchText: String = "",
)