package bassamalim.hidaya.features.quran

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.destinations.QuranSearcherUIDestination
import bassamalim.hidaya.features.destinations.QuranViewerUIDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class QuranVM @Inject constructor(
    private val repo: QuranRepo
): ViewModel() {

    private val suraNames = repo.getSuraNames()

    private val _uiState = MutableStateFlow(QuranState(
        favs = repo.getFavs(),
        tutorialDialogShown = repo.getShowTutorial()
    ))
    val uiState = _uiState.asStateFlow()

    fun onStart() {
        _uiState.update { it.copy(
            bookmarkedPageText = getBookmarkedPageText()
        )}
    }

    fun getItems(page: Int): List<Sura> {
        val listType = ListType.values()[page]

        val surat = repo.getSuraStr()

        val items = ArrayList<Sura>()
        val suras = repo.getAllSuras()
        for (i in suras.indices) {
            if (listType == ListType.Favorite && _uiState.value.favs[i] == 0) continue

            items.add(
                suras[i].let {
                    Sura(
                        it.sura_id, "$surat ${suraNames[it.sura_id]}",
                        it.search_name!!, it.tanzeel
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

    fun onSuraClick(suraId: Int, navigator: DestinationsNavigator) {
        navigator.navigate(
            QuranViewerUIDestination(
                type = "by_surah",
                suraId = suraId
            )
        )
    }

    fun onQuranSearcherClick(navigator: DestinationsNavigator) {
        navigator.navigate(QuranSearcherUIDestination)
    }

    fun onBookmarkedPageClick(navigator: DestinationsNavigator) {
        val bookmarkedPage = repo.getBookmarkedPage()
        if (bookmarkedPage != -1) {
            navigator.navigate(
                QuranViewerUIDestination(
                    type = "by_page",
                    pageNum = bookmarkedPage
                )
            )
        }
    }

    fun onFavClick(itemId: Int) {
        _uiState.update { it.copy(
            favs = _uiState.value.favs.toMutableList().apply {
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

    fun onSearchSubmit(navigator: DestinationsNavigator) {
        try {
            val num = _uiState.value.searchText.toInt()
            if (num in 1..604) {
                navigator.navigate(
                    QuranViewerUIDestination(
                        type = "by_page",
                        pageNum = num
                    )
                )
            }
            else {
                _uiState.update { it.copy(
                    shouldShowPageDNE = _uiState.value.shouldShowPageDNE + 1
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