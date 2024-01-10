package bassamalim.hidaya.features.books.bookSearcher.ui

data class BookSearcherUiState(
    val isLoading: Boolean = true,
    val searchText: String = "",
    val searched: Boolean = false,
    val matches: List<BookSearcherMatch>? = emptyList(),
    val maxMatches: Int = 10,
    val bookSelections: Map<Int, Boolean> = emptyMap(),
    val bookTitles: List<String> = emptyList(),
    val filtered: Boolean = false
)