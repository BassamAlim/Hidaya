package bassamalim.hidaya.features.bookSearcher

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.models.BookSearcherMatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class BookSearcherViewModel @Inject constructor(
    private val repository: BookSearcherRepository
): ViewModel() {

    val bookTitles: List<String>
    private var highlightColor: Color? = null

    private val _uiState = MutableStateFlow(BookSearcherState(
        maxMatchesItems = repository.getMaxMatchesItems(),
    ))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val bookSelections = repository.getBookSelections()
            _uiState.update { it.copy(
                language = repository.getLanguage(),
                numeralsLanguage = repository.getNumeralsLanguage(),
                bookSelections = bookSelections,
                filtered = bookSelections.containsValue(false),
                maxMatches = it.maxMatchesItems[repository.getMaxMatches()].toInt(),
            )}
        }

        bookTitles = repository.getBookTitles(_uiState.value.language)
    }

    fun onFilterClick() {
        _uiState.update { it.copy(
            filterDialogShown = true
        )}
    }

    fun onFilterDialogDismiss(selections: Map<Int, Boolean>) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                filterDialogShown = false,
                bookSelections = selections,
                filtered = selections.containsValue(false)
            )}
        }

        highlightColor?.let { search(it) }  // re-search if already searched
    }

    fun onMaxMatchesIndexChange(index: Int) {
        _uiState.update { it.copy(
            maxMatches = it.maxMatchesItems[index].toInt()
        )}

        highlightColor?.let { search(it) }  // re-search if already searched

        viewModelScope.launch {
            repository.setMaxMatches(index)
        }
    }

    fun search(highlightColor: Color) {
        this.highlightColor = highlightColor

        val matches = ArrayList<BookSearcherMatch>()

        val bookContents = repository.getBookContents()
        for (i in bookContents.indices) {
            if (!_uiState.value.bookSelections[i]!! || !repository.isDownloaded(i))
                continue

            val bookContent = bookContents[i]
            for (j in bookContent.chapters.indices) {
                val chapter = bookContent.chapters[j]

                for (k in chapter.doors.indices) {
                    val door = chapter.doors[k]
                    val doorText = door.text

                    val matcher = Pattern.compile(_uiState.value.searchText).matcher(doorText)
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
                                bookId = i,
                                bookTitle = bookContent.bookInfo.bookTitle,
                                chapterId = j,
                                chapterTitle = chapter.chapterTitle,
                                doorId = k,
                                doorTitle = door.doorTitle,
                                text = annotatedString
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
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}