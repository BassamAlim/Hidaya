package bassamalim.hidaya.features.quran.surasMenu.ui

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.quran.reader.domain.QuranTarget
import bassamalim.hidaya.features.quran.surasMenu.domain.QuranSurasDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private var numeralsLanguage: Language? = null
    private lateinit var suraNames: List<String>
    private lateinit var allSurasFlow: Flow<List<Sura>>

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
            suraNames = domain.getSuraNames(language)

            _uiState.update { it.copy(
                isLoading = false,
                isTutorialDialogShown = domain.getShouldShowTutorial()
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
    }

    fun onQuranSearcherClick() {
        navigator.navigate(Screen.QuranSearcher)
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

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

    fun onSearchSubmit(snackbarHostState: SnackbarHostState, message: String) {
        try {
            val num = _uiState.value.searchText.toInt()
            if (num in 1..604) {
                navigator.navigate(
                    Screen.QuranReader(
                        targetType = QuranTarget.PAGE.name,
                        targetValue = num.toString()
                    )
                )
            }
            else {
                viewModelScope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        } catch (_: NumberFormatException) {}
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            isTutorialDialogShown = false
        )}

        if (doNotShowAgain) {
            viewModelScope.launch {
                domain.setDoNotShowTutorialAgain()
            }
        }
    }

    fun getItems(page: Int): Flow<List<Sura>> {
        val menuType = MenuType.entries[page]

        return allSurasFlow.map { suras ->
            val items = suras.filter { sura ->
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

            if (_uiState.value.searchText.isEmpty()) items
            else items.filter {
                it.plainName.contains(_uiState.value.searchText, true)
            }
        }
    }

}