package bassamalim.hidaya.features.quran.surasMenu.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.graphics.Color
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.data.dataSources.room.entities.Verse
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quran.reader.domain.QuranTarget
import bassamalim.hidaya.features.quran.surasMenu.domain.QuranSurasDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranSurasViewModel @Inject constructor(
    private val domain: QuranSurasDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var language: Language
    private lateinit var numeralsLanguage: Language
    private lateinit var suraNames: List<String>
    private lateinit var allSurasFlow: Flow<List<Sura>>
    private lateinit var allSuras: List<Sura>
    private lateinit var allVerses: List<Verse>
    private var highlightColor: Color? = null

    private val _uiState = MutableStateFlow(QuranSurasUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getBookmarks()
    ) { state, bookmarks ->
        if (state.isLoading) state
        else state.copy(bookmarks = bookmarks)
    }.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = QuranSurasUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            language = domain.getLanguage()
            numeralsLanguage = domain.getNumeralsLanguage()
            allSurasFlow = domain.getAllSuras(language)
            allSuras = allSurasFlow.first()
            suraNames = domain.getSuraNames(language)
            allVerses = domain.getAllVerses()

            _uiState.update { it.copy(
                isLoading = false
            )}
        }
    }

    fun onSuraClick(suraId: Int) {
        navigator.navigate(
            Screen.QuranReader(
                targetType = QuranTarget.SURA.name,
                targetValue = suraId.toString()
            )
        )

        domain.trackSuraViewed(suraNames[suraId])
    }

    fun onPageClick(pageNum: String) {
        navigator.navigate(
            Screen.QuranReader(
                targetType = QuranTarget.PAGE.name,
                targetValue = pageNum
            )
        )
    }

    fun onBookmarksClick() {
        _uiState.update { it.copy(
            isBookmarksExpanded = !it.isBookmarksExpanded
        )}
    }

    fun onBookmarkOptionClick(
        verseId: Int?,
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        if (verseId == null) {
            viewModelScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
        else {
            navigator.navigate(
                Screen.QuranReader(
                    targetType = QuranTarget.VERSE.name,
                    targetValue = verseId.toString()
                )
            )
        }
    }

    fun onFavoriteClick(itemId: Int, oldState: Boolean) {
        viewModelScope.launch {
            domain.setFav(suraId = itemId, fav = !oldState)
        }
    }

    fun getItems(page: Int): Flow<List<Sura>> {
        val menuType = MenuType.entries[page]

        return allSurasFlow.map { suras ->
            suras.filter { sura ->
                !(menuType == MenuType.FAVORITES && !sura.isFavorite)
            }.map { sura ->
                Sura(
                    id = sura.id,
                    decoratedName = suraNames[sura.id],
                    plainName = sura.plainName,
                    revelation = sura.revelation,
                    isFavorite = sura.isFavorite
                )
            }
        }
    }

    fun searchSurasAndPages(query: String): List<SearchMatch> {
        return if (query.isEmpty()) {
            allSuras.take(3).map { sura ->
                SuraMatch(
                    id = sura.id,
                    decoratedName = suraNames[sura.id],
                    plainName = sura.plainName,
                    isFavorite = sura.isFavorite
                )
            }
        }
        else if (query.isDigitsOnly()) {
            val num = query.toInt()
            if (num in 1..604) {
                val pageSuraId = allVerses.first { verse -> verse.pageNum == num }.suraNum - 1
                listOf(
                    PageMatch(
                        num = translateNums(
                            string = query,
                            numeralsLanguage = numeralsLanguage
                        ),
                        suraName = suraNames[pageSuraId]
                    )
                )
            }
            else emptyList()
        }
        else {
            domain.searchSuras(
                query = query,
                items = allSuras,
                limit = 3
            )
        }
    }

    fun searchVerses(query: String, highlightColor: Color): List<VerseMatch> {
        this.highlightColor = highlightColor

        return domain.searchVerses(
            query = query,
            items = allVerses,
            suraNames = suraNames,
            numeralsLanguage = numeralsLanguage,
            highlightColor = highlightColor
        )
    }

    fun onVerseClick(verseId: Int) {
        navigator.navigate(
            Screen.QuranReader(
                targetType = QuranTarget.VERSE.name,
                targetValue = verseId.toString()
            )
        )
    }

}