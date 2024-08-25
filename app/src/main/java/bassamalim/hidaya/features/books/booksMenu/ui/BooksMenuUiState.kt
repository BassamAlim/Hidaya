package bassamalim.hidaya.features.books.booksMenu.ui

import bassamalim.hidaya.core.data.database.models.Book
import bassamalim.hidaya.core.enums.DownloadState

data class BooksMenuUiState(
    val items: List<Book> = emptyList(),
    val downloadStates: Map<Int, DownloadState> = emptyMap(),
    val shouldShowWait: Int = 0,
    val tutorialDialogShown: Boolean = false
)