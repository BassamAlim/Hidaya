package bassamalim.hidaya.features.books

import bassamalim.hidaya.core.data.database.dbs.BooksDB
import bassamalim.hidaya.core.enums.DownloadState

data class BooksState(
    val items: List<BooksDB> = emptyList(),
    val downloadStates: List<DownloadState> = emptyList(),
    val shouldShowWait: Int = 0,
    val tutorialDialogShown: Boolean = false
)
