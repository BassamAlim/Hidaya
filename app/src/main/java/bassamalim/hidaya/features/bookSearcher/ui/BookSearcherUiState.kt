package bassamalim.hidaya.features.bookSearcher.ui

data class BookSearcherUiState(
    val searchText: String = "",
    val matches: List<BookSearcherMatch>? = null,
    val maxMatchesItems: Array<String> = emptyArray(),
    val maxMatches: Int = 10,
    val bookSelections: Map<Int, Boolean> = emptyMap(),
    val bookTitles: List<String> = emptyList(),
    val filtered: Boolean = false,
    val filterDialogShown: Boolean = false
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookSearcherUiState

        if (searchText != other.searchText) return false
        if (matches != other.matches) return false
        if (!maxMatchesItems.contentEquals(other.maxMatchesItems)) return false
        if (maxMatches != other.maxMatches) return false
        if (bookSelections != other.bookSelections) return false
        if (bookTitles != other.bookTitles) return false
        if (filtered != other.filtered) return false
        if (filterDialogShown != other.filterDialogShown) return false

        return true
    }

    override fun hashCode(): Int {
        var result = searchText.hashCode()
        result = 31 * result + (matches?.hashCode() ?: 0)
        result = 31 * result + maxMatchesItems.contentHashCode()
        result = 31 * result + maxMatches
        result = 31 * result + bookSelections.hashCode()
        result = 31 * result + bookTitles.hashCode()
        result = 31 * result + filtered.hashCode()
        result = 31 * result + filterDialogShown.hashCode()
        return result
    }

}