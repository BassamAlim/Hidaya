package bassamalim.hidaya.features.books.ui

import bassamalim.hidaya.core.data.database.dbs.BooksDB
import bassamalim.hidaya.core.enums.DownloadState

data class BooksUiState(
    val items: List<BooksDB> = emptyList(),
    val downloadStates: Map<Int, DownloadState> = emptyMap(),
    val shouldShowWait: Int = 0,
    val tutorialDialogShown: Boolean = false
)
