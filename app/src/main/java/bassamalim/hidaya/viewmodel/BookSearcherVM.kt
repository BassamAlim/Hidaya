package bassamalim.hidaya.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.models.BookSearcherMatch
import bassamalim.hidaya.repository.BookSearcherRepo
import bassamalim.hidaya.state.BookSearcherState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class BookSearcherVM @Inject constructor(
    private val repository: BookSearcherRepo
): ViewModel() {

    var searchText = mutableStateOf("")
        private set
    var bookSelections = repository.getBookSelections()
    var maxMatchesIndex = mutableStateOf(repository.getMaxMatchesIndex())
        private set
    var maxMatchesItems = repository.getMaxMatchesItems()
    val bookTitles = repository.getBookTitles()

    private val _uiState = MutableStateFlow(BookSearcherState(
        filtered = bookSelections.contains(false)
    ))
    val uiState = _uiState.asStateFlow()

    fun onFilterClick() {
        _uiState.update { it.copy(
            filterDialogShown = true
        )}
    }

    fun onFilterDialogDismiss(selections: Array<Boolean>) {
        bookSelections = selections

        _uiState.update { it.copy(
            filtered = bookSelections.contains(false),
            filterDialogShown = false
        )}
    }

    fun onMaxMatchesIndexChange(index: Int) {
        maxMatchesIndex.value = index

        repository.updateMaxMatchesIndex(index)
    }

    fun search(highlightColor: Color) {
        val matches = ArrayList<BookSearcherMatch>()

        val bookContents = repository.getBookContents()
        for (i in bookContents.indices) {
            if (!bookSelections[i] || !repository.isDownloaded(i)) continue

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

                        if (matches.size == maxMatchesItems[maxMatchesIndex.value].toInt()) {
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

}