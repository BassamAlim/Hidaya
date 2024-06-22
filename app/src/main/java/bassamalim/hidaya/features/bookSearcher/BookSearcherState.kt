package bassamalim.hidaya.features.bookSearcher

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.BookSearcherMatch

data class BookSearcherState(
    val searchText: String = "",
    val matches: List<BookSearcherMatch> = emptyList(),
    val noResultsFound: Boolean = false,
    val maxMatches: Int = 10,
    val maxMatchesItems: Array<String> = emptyArray(),
    val bookSelections: Map<Int, Boolean> = emptyMap(),
    val filtered: Boolean = false,
    val language: Language = Language.ARABIC,
    val numeralsLanguage: Language = Language.ARABIC,
    val filterDialogShown: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookSearcherState

        if (searchText != other.searchText) return false
        if (matches != other.matches) return false
        if (noResultsFound != other.noResultsFound) return false
        if (maxMatches != other.maxMatches) return false
        if (!maxMatchesItems.contentEquals(other.maxMatchesItems)) return false
        if (filtered != other.filtered) return false
        if (language != other.language) return false
        if (numeralsLanguage != other.numeralsLanguage) return false
        if (filterDialogShown != other.filterDialogShown) return false

        return true
    }

    override fun hashCode(): Int {
        var result = searchText.hashCode()
        result = 31 * result + matches.hashCode()
        result = 31 * result + noResultsFound.hashCode()
        result = 31 * result + maxMatches
        result = 31 * result + maxMatchesItems.contentHashCode()
        result = 31 * result + filtered.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + numeralsLanguage.hashCode()
        result = 31 * result + filterDialogShown.hashCode()
        return result
    }
}
