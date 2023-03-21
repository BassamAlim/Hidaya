package bassamalim.hidaya.features.bookSearcher

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.models.BookSearcherMatch
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class BookSearcherVM @Inject constructor(
    private val repo: BookSearcherRepo
): ViewModel() {

    var searchText = mutableStateOf("")
        private set
    var bookSelections = repo.getBookSelections()
    val maxMatchesItems = repo.getMaxMatchesItems()
    val translatedMaxMatchesItems = maxMatchesItems.map {
        translateNums(repo.numeralsLanguage, it)
    }.toTypedArray()
    val bookTitles = repo.getBookTitles()
    private var highlightColor: Color? = null

    private val _uiState = MutableStateFlow(
        BookSearcherState(
        maxMatches = maxMatchesItems[repo.getMaxMatchesIndex()].toInt(),
        filtered = bookSelections.contains(false)
    )
    )
    val uiState = _uiState.asStateFlow()

    fun onFilterClick() {
        _uiState.update { it.copy(
            filterDialogShown = true
        )}
    }

    fun onFilterDialogDismiss(selections: Array<Boolean>) {
        bookSelections = selections

        _uiState.update { it.copy(
            filterDialogShown = false,
            filtered = bookSelections.contains(false)
        )}

        highlightColor ?.let { search(it) }  // re-search if already searched
    }

    fun onMaxMatchesIndexChange(index: Int) {
        _uiState.update { it.copy(
            maxMatches = maxMatchesItems[index].toInt()
        )}

        highlightColor ?.let { search(it) }  // re-search if already searched

        repo.setMaxMatchesIndex(index)
    }

    fun search(highlightColor: Color) {
        this.highlightColor = highlightColor

        val matches = ArrayList<BookSearcherMatch>()

        val bookContents = repo.getBookContents()
        for (i in bookContents.indices) {
            if (!bookSelections[i] || !repo.isDownloaded(i)) continue

            val bookContent = bookContents[i]
            for (j in bookContent.chapters.indices) {
                val chapter = bookContent.chapters[j]

                for (k in chapter.doors.indices) {
                    val door = chapter.doors[k]
                    val doorText = door.text

                    val matcher = Pattern.compile(searchText.value).matcher(doorText)
                    if (matcher.find()) {
                        val annotatedString = buildAnnotatedString {
                            append(doorText)

                            do {
                                addStyle(
                                    style = SpanStyle(color = highlightColor),
                                    start = matcher.start(),
                                    end = matcher.end()
                                )
                            } while (matcher.find())
                        }

                        matches.add(
                            BookSearcherMatch(
                                i, bookContent.bookInfo.bookTitle,
                                j, chapter.chapterTitle,
                                k, door.doorTitle, annotatedString
                            )
                        )

                        if (matches.size == _uiState.value.maxMatches) {
                            _uiState.update { it.copy(
                                matches = matches,
                                noResultsFound = false
                            )}
                            return
                        }
                    }
                }
            }
        }

        _uiState.update { it.copy(
            matches = matches,
            noResultsFound = matches.isEmpty()
        )}
    }

    fun onSearchTextChange(text: String) {
        searchText.value = text
    }

}