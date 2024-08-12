package bassamalim.hidaya.features.quran.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quran.domain.QuranDomain
import bassamalim.hidaya.features.quranReader.domain.QuranTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val domain: QuranDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var language: Language
    private var numeralsLanguage: Language? = null
    private lateinit var suraNames: List<String>
    private var bookmarkedPage = -1

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getBookmark()
    ) { state, bookmark ->
        bookmarkedPage = bookmark?.pageNum ?: -1
        state.copy(
            bookmarkPageText =
                if (bookmark == null || numeralsLanguage == null) null
                else translateNums(
                    numeralsLanguage = numeralsLanguage!!,
                    string = bookmark.pageNum.toString()
                ),
            bookmarkSuraText =
                if (bookmark == null) null
                else suraNames[bookmark.suraId]
        )
    }.stateIn(
        initialValue = QuranUiState(),
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

    fun onSuraClick(suraId: Int) {
        navigator.navigate(
            Screen.QuranViewer(
                targetType = QuranTarget.SURA.name,
                targetValue = suraId.toString()
            )
        )
    }

    fun onQuranSearcherClick() {
        navigator.navigate(Screen.QuranSearcher)
    }

    fun onBookmarkedPageClick() {
        if (bookmarkedPage != -1) {
            navigator.navigate(
                Screen.QuranViewer(
                    targetType = QuranTarget.PAGE.name,
                    targetValue = bookmarkedPage.toString()
                )
            )
        }
    }

    fun onFavClick(itemId: Int) {
        _uiState.update { it.copy(
            favs = it.favs.toMutableMap().apply {
                this[itemId] = !it.favs[itemId]!!
            }
        )}

        viewModelScope.launch {
            domain.setFav(
                suraId = itemId,
                fav = if (_uiState.value.favs[itemId]!!) 1 else 0
            )
        }
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

    fun onSearchSubmit() {
        try {
            val num = _uiState.value.searchText.toInt()
            if (num in 1..604) {
                navigator.navigate(
                    Screen.QuranViewer(
                        targetType = QuranTarget.PAGE.name,
                        targetValue = num.toString()
                    )
                )
            }
            else {
                _uiState.update { it.copy(
                    shouldShowPageDoesNotExist = it.shouldShowPageDoesNotExist + 1
                )}
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

    fun getItems(page: Int): List<Sura> {
        val listType = ListType.entries[page]

        val items = ArrayList<Sura>()
        val suar = domain.getAllSuar()
        for (i in suar.indices) {
            if (listType == ListType.FAVORITES && !_uiState.value.favs[i]!!) continue

            items.add(
                suar[i].let {
                    Sura(
                        id = it.id,
                        suraName = suraNames[it.id],
                        searchName = it.plainNameAr,
                        tanzeel = it.revelation
                    )
                }
            )
        }

        return if (_uiState.value.searchText.isEmpty()) items
        else items.filter {
            it.searchName.contains(_uiState.value.searchText, true)
        }
    }

}