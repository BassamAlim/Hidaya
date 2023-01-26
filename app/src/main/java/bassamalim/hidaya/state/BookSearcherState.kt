package bassamalim.hidaya.state

import bassamalim.hidaya.models.BookSearcherMatch

data class BookSearcherState(
    val matches: List<BookSearcherMatch> = emptyList(),
    val noResultsFound: Boolean = false,
    val maxMatches: Int = 10,
    val filtered: Boolean = false,
    val filterDialogShown: Boolean = false
)
