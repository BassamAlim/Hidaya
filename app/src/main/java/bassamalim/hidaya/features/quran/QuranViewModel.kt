package bassamalim.hidaya.features.quran

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quranReader.QuranTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val repo: QuranRepository,
    private val navigator: Navigator
): ViewModel() {

    private val suraNames = repo.getSuraNames()

    private val _uiState = MutableStateFlow(QuranState(
        favs = repo.getFavs(),
        tutorialDialogShown = repo.getShowTutorial()
    ))
    val uiState = _uiState.asStateFlow()

    fun onStart() {
        _uiState.update { it.copy(
            bookmarkedPageText = getBookmarkedPageText(),
            favs = repo.getFavs()
        )}
    }

    fun getItems(page: Int): List<Sura> {
        val listType = ListType.entries[page]

        val surat = repo.getSuraStr()

        val items = ArrayList<Sura>()
        val suar = repo.getAllSuar()
        for (i in suar.indices) {
            if (listType == ListType.FAVORITES && _uiState.value.favs[i] == 0) continue

            items.add(
                suar[i].let {
                    Sura(
                        it.suraId, "$surat ${suraNames[it.suraId]}",
                        it.searchName, it.tanzeel
                    )
                }
            )
        }

        return if (_uiState.value.searchText.isEmpty()) items
        else items.filter {
            it.searchName.contains(_uiState.value.searchText, true)
        }
    }

    private fun getBookmarkedPageText(): String {
        val bookmarkedSura = repo.getBookmarkedSura()
        val bookmarkedPage = repo.getBookmarkedPage()

        return if (bookmarkedPage == -1 || bookmarkedSura == -1)
            repo.getNoBookmarkedPageStr()
        else {
            "${repo.getBookmarkedPageStr()} " +
                    "${repo.getPageStr()} " +
                    "${translateNums(
                        repo.getNumeralsLanguage(), bookmarkedPage.toString()
                    )}, " +
                    "${repo.getSuraStr()} ${repo.getSuraNames()[bookmarkedSura]}"
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
        val bookmarkedPage = repo.getBookmarkedPage()
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
            favs = it.favs.toMutableList().apply {
                this[itemId] = if (this[itemId] == 1) 0 else 1
            }
        )}

        repo.setFav(itemId, _uiState.value.favs[itemId])
        repo.updateFavorites(_uiState.value.favs.toList())
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
                    shouldShowPageDNE = it.shouldShowPageDNE + 1
                )}
            }
        } catch (_: NumberFormatException) {}
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            tutorialDialogShown = false
        )}

        if (doNotShowAgain) repo.setDoNotShowAgain()
    }

}