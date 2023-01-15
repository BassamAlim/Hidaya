package bassamalim.hidaya.state

import bassamalim.hidaya.database.dbs.BooksDB

data class BooksState(
    val items: List<BooksDB> = emptyList(),
    val shouldShowWaitMassage: Boolean = false,
    val isTutorialDialogShown: Boolean = false
)
