package bassamalim.hidaya.features.books.booksMenu

data class BooksMenuUiState(
    val isLoading: Boolean = true,
    val books: Map<Int, Book> = emptyMap(),
    val shouldShowWait: Int = 0,
    val isTutorialActive: Boolean = false
)
