package bassamalim.hidaya.features.books.bookSearcher.ui

data class BookSearcherUiState(
    val searchText: String = "",
    val matches: List<BookSearcherMatch>? = null,
    val maxMatches: Int = 10,
    val bookSelections: Map<Int, Boolean> = emptyMap(),
    val bookTitles: List<String> = emptyList(),
    val filtered: Boolean = false,
    val filterDialogShown: Boolean = false
)