package bassamalim.hidaya.features.books.booksMenu.ui

data class BooksMenuUiState(
    val isLoading: Boolean = true,
    val books: Map<Int, Book> = emptyMap(),
    val shouldShowWait: Int = 0,
    val shouldShowNoBooksDownloaded: Int = 0,
    val tutorialDialogShown: Boolean = false
)
