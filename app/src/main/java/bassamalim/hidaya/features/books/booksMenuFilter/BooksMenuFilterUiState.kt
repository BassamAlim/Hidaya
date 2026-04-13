package bassamalim.hidaya.features.books.booksMenuFilter

data class BooksMenuFilterUiState(
    val isLoading: Boolean = true,
    val options: Map<Int, BooksMenuFilterItem> = emptyMap()
)
