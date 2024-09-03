package bassamalim.hidaya.features.quran.quranSearcher.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quran.quranSearcher.domain.QuranSearcherDomain
import bassamalim.hidaya.features.quran.quranReader.domain.QuranTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class QuranSearcherViewModel @Inject constructor(
    private val domain: QuranSearcherDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var language: Language
    private lateinit var numeralsLanguage: Language
    private var allVerses = domain.getAllVerses()
    private lateinit var suraNames: List<String>
    private var highlightColor: Color? = null

    private val _uiState = MutableStateFlow(QuranSearcherUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getMaxMatches()
    ) { state, maxMatches -> state.copy(
        maxMatches = maxMatches
    )}.stateIn(
        initialValue = QuranSearcherUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    init {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()
            suraNames = domain.getSuraNames(language)
        }
    }

    private fun search(highlightColor: Color) {
        this.highlightColor = highlightColor

        val matches = mutableListOf<QuranSearcherMatch>()

        for (i in allVerses.indices) {
            val a = allVerses[i]
            val string = a.plainText

            val matcher = Pattern.compile(_uiState.value.searchText).matcher(string)
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
                        id = a.id,
                        verseNum = translateNums(
                            string = a.num.toString(),
                            numeralsLanguage = numeralsLanguage
                        ),
                        suraName = suraNames[a.suraNum-1],
                        pageNum = translateNums(
                            string = a.pageNum.toString(),
                            numeralsLanguage = numeralsLanguage
                        ),
                        text = annotatedString,
                        interpretation = a.interpretation
                    )
                )

                if (matches.size == _uiState.value.maxMatches) {
                    _uiState.update { it.copy(
                        matches = matches,
                        isNoResultsFound = false
                    )}
                    return
                }
            }
        }

        _uiState.update { it.copy(
            matches = matches,
            isNoResultsFound = matches.isEmpty()
        )}
    }

    fun onSearchValueChange(value: String, highlightColor: Color) {
        _uiState.update { it.copy(
            searchText = value
        )}

        search(highlightColor)
    }

    fun onMaxMatchesChange(value: Int) {
        _uiState.update { it.copy(
            maxMatches = value
        )}

        highlightColor ?.let { search(it) }  // re-search if already searched

        viewModelScope.launch {
            domain.setMaxMatches(value)
        }
    }

    fun onGotoPageClick(match: QuranSearcherMatch) {
        navigator.navigate(
            Screen.QuranViewer(
                targetType = QuranTarget.VERSE.name,
                targetValue = match.id.toString()
            )
        )
    }

}