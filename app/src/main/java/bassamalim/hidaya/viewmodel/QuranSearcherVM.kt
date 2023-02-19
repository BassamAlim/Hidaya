package bassamalim.hidaya.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.nav.Screen
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.models.QuranSearcherMatch
import bassamalim.hidaya.repository.QuranSearcherRepo
import bassamalim.hidaya.state.QuranSearcherState
import bassamalim.hidaya.utils.LangUtils.translateNums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class QuranSearcherVM @Inject constructor(
    private val repo: QuranSearcherRepo
): ViewModel() {

    val numeralsLanguage = repo.numeralsLanguage
    private var allAyat = repo.getAyat()
    private var names =
        if (repo.language == Language.ENGLISH) repo.getSuraNamesEn()
        else repo.getSuraNames()
    var maxMatchesItems = repo.getMaxMatchesItems()
    val translatedMaxMatchesItems = maxMatchesItems.map {
        translateNums(numeralsLanguage, it)
    }.toTypedArray()
    var searchText = ""
        private set
    private var highlightColor: Color? = null

    private val _uiState = MutableStateFlow(QuranSearcherState(
        maxMatches = maxMatchesItems[repo.getMaxMatchesIndex()].toInt()
    ))
    val uiState = _uiState.asStateFlow()

    fun search(highlightColor: Color) {
        this.highlightColor = highlightColor

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

        highlightColor ?.let { search(it) }  // re-search if already searched

        repo.setMaxMatchesIndex(index)
    }

    fun onGotoPageClick(page: Int, navController: NavController) {
        navController.navigate(
            Screen.QuranViewer(
                "by_page",
                page.toString()
            ).route
        )
    }

}