package bassamalim.hidaya.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.Sura
import bassamalim.hidaya.repository.QuranRepo
import bassamalim.hidaya.state.QuranState
import bassamalim.hidaya.utils.LangUtils.translateNums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class QuranVM @Inject constructor(
    private val repository: QuranRepo
): ViewModel() {

    private val names = repository.getSuraNames()
    var searchText by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(QuranState(
        bookmarkedPageText = getBookmarkedPageText(),
        items = getItems(ListType.All),
        favs = repository.getFavs()
    ))
    val uiState = _uiState.asStateFlow()

    fun onStart() {
        _uiState.update { it.copy(
            bookmarkedPageText = getBookmarkedPageText()
        )}
    }

    private fun getItems(type: ListType): List<Sura> {
        val surat = repository.getSuraStr()

        val items = ArrayList<Sura>()
        val suras = repository.getAllSuras()
        for (i in suras.indices) {
            if (type == ListType.Favorite && _uiState.value.favs[i] == 0) continue

            val sura = suras[i]
            items.add(
                Sura(
                    sura.sura_id, "$surat ${names[sura.sura_id]}",
                    sura.search_name!!, sura.tanzeel
                )
            )
        }
        return items
    }

    private fun getBookmarkedPageText(): String {
        val bookmarkedSura = repository.getBookmarkedSura()
        val bookmarkedPage = repository.getBookmarkedPage()

        return if (bookmarkedPage == -1) return repository.getNoBookmarkedPageStr()
        else {
            "${repository.getBookmarkedPageStr()} " +
                    "${repository.getPageStr()} " +
                    "${translateNums(
                        repository.getNumeralsLanguage(), bookmarkedPage.toString()
                    )}, " +
                    "${repository.getSuraStr()} ${repository.getSuraNames()[bookmarkedSura]}"
        }
    }

    fun onSuraClick(suraID: Int, navController: NavController) {
        navController.navigate(
            Screen.QuranViewer(
                "by_surah",
                suraID.toString()
            ).route
        )
    }

    fun onQuranSearcherClick(navController: NavController) {
        navController.navigate(Screen.QuranSearcher.route)
    }

    fun onBookmarkedPageClick(navController: NavController) {
        val bookmarkedPage = repository.getBookmarkedPage()
        if (bookmarkedPage != -1) {
            navController.navigate(
                Screen.QuranViewer(
                    "by_page",
                    bookmarkedPage.toString()
                ).route
            )
        }
    }

    fun onListTypeChange(pageNum: Int) {
        _uiState.update { it.copy(
            items = getItems(ListType.values()[pageNum])
        )}
    }

    fun onFavClick(itemId: Int) {
        val mutableFavs = _uiState.value.favs.toMutableList()
        mutableFavs[itemId] = if (mutableFavs[itemId] == 1) 0 else 1

        _uiState.update { it.copy(
            favs = mutableFavs
        )}

        repository.updateFavorites(mutableFavs.toList())
    }

    fun onSearchTextChange(text: String) {
        searchText = text
    }

    fun onSearchSubmit(navController: NavController) {
        try {
            val num = searchText.toInt()
            if (num in 1..604) {
                navController.navigate(
                    Screen.QuranViewer(
                        "by_page",
                        num.toString()
                    ).route
                )
            }
            else {
                _uiState.update { it.copy(
                    shouldShowPageDNE = true
                )}
            }
        } catch (_: NumberFormatException) {}
    }

    fun onTutorialDialogDismiss(doNotShowAgain: Boolean) {
        _uiState.update { it.copy(
            isTutorialDialogShown = false
        )}

        if (doNotShowAgain) repository.setDoNotShowAgain()
    }

}