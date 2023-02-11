package bassamalim.hidaya.state

import bassamalim.hidaya.database.dbs.BooksDB
import bassamalim.hidaya.enums.DownloadState

data class BooksState(
    val items: List<BooksDB> = emptyList(),
    val downloadStates: List<DownloadState> = emptyList(),
    val shouldShowWait: Int = 0,
    val tutorialDialogShown: Boolean = false
)
