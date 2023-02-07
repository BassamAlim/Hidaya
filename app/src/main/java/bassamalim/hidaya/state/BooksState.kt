package bassamalim.hidaya.state

import bassamalim.hidaya.database.dbs.BooksDB

data class BooksState(
    val items: List<BooksDB> = emptyList(),
    val shouldShowWait: Int = 0,
    val tutorialDialogShown: Boolean = false
)
