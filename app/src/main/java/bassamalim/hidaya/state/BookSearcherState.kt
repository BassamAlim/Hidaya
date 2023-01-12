package bassamalim.hidaya.state

import bassamalim.hidaya.models.BookSearcherMatch

data class BookSearcherState(
    val matches: List<BookSearcherMatch> = emptyList(),
    val noResultsFound: Boolean = false,
    val filtered: Boolean = false,
    val filterDialogShown: Boolean = false
)
