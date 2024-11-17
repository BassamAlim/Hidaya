package bassamalim.hidaya.features.quran.searcher.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.data.dataSources.room.entities.Verse
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.ui.theme.uthmanic_hafs
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quran.reader.domain.QuranTarget
import bassamalim.hidaya.features.quran.searcher.domain.QuranSearcherDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
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
    private lateinit var allVerses: List<Verse>
    private lateinit var suraNames: List<String>
    private var highlightColor: Color? = null

    private val _uiState = MutableStateFlow(QuranSearcherUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getMaxMatches()
    ) { state, maxMatches ->
        state.copy(
            maxMatches = maxMatches
        )
    }.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = QuranSearcherUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()
            allVerses = domain.getAllVerses()
            suraNames = domain.getSuraNames(language)
        }
    }

    private fun search(highlightColor: Color) {
        this.highlightColor = highlightColor

        val matches = mutableListOf<QuranSearcherMatch>()

        for (verse in allVerses) {
            val string = verse.plainText

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
                        id = verse.id,
                        verseNum = translateNums(
                            string = verse.num.toString(),
                            numeralsLanguage = numeralsLanguage
                        ),
                        suraName = suraNames[verse.suraNum-1],
                        pageNum = translateNums(
                            string = verse.pageNum.toString(),
                            numeralsLanguage = numeralsLanguage
                        ),
                        text = annotatedString,
                        interpretation = annotateInterpretation(verse.interpretation)
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
            Screen.QuranReader(
                targetType = QuranTarget.VERSE.name,
                targetValue = match.id.toString()
            )
        )
    }

    private fun annotateInterpretation(interpretation: String): AnnotatedString {
        return buildAnnotatedString {
            val regex = Regex("(<aya>(.*?)</aya>)")
            var lastIndex = 0

            regex.findAll(interpretation).forEach { matchResult ->
                val ayaStartIndex = matchResult.range.first
                val ayaEndIndex = matchResult.range.last + 1

                append(interpretation.substring(lastIndex, ayaStartIndex))

                pushStyle(SpanStyle(fontFamily = uthmanic_hafs))
                append(matchResult.groups[2]?.value.orEmpty()) // Groups[2] contains the text within the tags
                pop()

                lastIndex = ayaEndIndex
            }

            if (lastIndex < interpretation.length) {
                append(interpretation.substring(lastIndex))
            }
        }
    }

}