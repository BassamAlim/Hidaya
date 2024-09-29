package bassamalim.hidaya.features.books.booksMenuFilter.ui

import bassamalim.hidaya.features.books.booksMenuFilter.domain.BooksMenuFilterItem

data class BooksMenuFilterUiState(
    val isLoading: Boolean = true,
    val options: Map<Int, BooksMenuFilterItem> = emptyMap()
)
