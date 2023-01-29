package bassamalim.hidaya.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import bassamalim.hidaya.enum.Language
import bassamalim.hidaya.models.QuranSearcherMatch
import bassamalim.hidaya.repository.QuranSearcherRepo
import bassamalim.hidaya.state.QuranSearcherState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class QuranSearcherVM @Inject constructor(
    private val repository: QuranSearcherRepo
): ViewModel() {

    val numeralsLanguage = repository.numeralsLanguage
    private var allAyat = repository.getAyat()
    private var names =
        if (repository.language == Language.ENGLISH) repository.getSuraNamesEn()
        else repository.getSuraNames()
    var maxMatchesItems =
        if (repository.language == Language.ENGLISH) repository.getMaxMatchesItemsEn()
        else repository.getMaxMatchesItems()
    var searchText = ""
        private set

    private val _uiState = MutableStateFlow(QuranSearcherState(
        maxMatches = maxMatchesItems[repository.getMaxMatchesIndex()].toInt()
    ))
    val uiState = _uiState.asStateFlow()

    fun search(highlightColor: Color) {
        val matches = ArrayList<QuranSearcherMatch>()

        for (i in allAyat.indices) {
            val a = allAyat[i]
            val string = a.aya_text_emlaey

            val matcher = Pattern.compile(searchText).matcher(string)
            if (matcher.find()) {
                val annotatedString = buildAnnotatedString {
                    append(string)

                    do {
                        addStyle(
                            style = SpanStyle(highlightColor),
                            start = matcher.start(),
                            end = matcher.end()
                        )
                    } while (matcher.find())
                }

                matches.add(
                    QuranSearcherMatch(
                        a.sura_num, a.aya_num, names[a.sura_num-1], a.page,
                        annotatedString, a.aya_tafseer
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

        _uiState.update { it.copy(
            matches = matches,
            noResultsFound = matches.isEmpty()
        )}
    }

    fun onSearchValueChange(value: String, highlightColor: Color) {
        searchText = value

        search(highlightColor)
    }

    fun onMaxMatchesIndexChange(index: Int) {
        _uiState.update { it.copy(
            maxMatches = maxMatchesItems[index].toInt()
        )}

        repository.setMaxMatchesIndex(index)
    }

    fun onGotoPageClick(page: Int, navController: NavController) {
        navController.navigate(
            Screen.QuranViewer.withArgs(
                "by_page",
                page.toString()
            )
        )
    }

}