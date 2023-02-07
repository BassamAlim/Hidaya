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
    private val repo: QuranRepo
): ViewModel() {

    private var listType = ListType.All
    private val names = repo.getSuraNames()
    var searchText by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(QuranState(
        items = getItems(listType),
        favs = repo.getFavs()
    ))
    val uiState = _uiState.asStateFlow()

    fun onStart() {
        _uiState.update { it.copy(
            bookmarkedPageText = getBookmarkedPageText()
        )}
    }

    private fun getItems(type: ListType): List<Sura> {
        val surat = repo.getSuraStr()

        val items = ArrayList<Sura>()
        val suras = repo.getAllSuras()
        for (i in suras.indices) {
            if (type == ListType.Favorite && _uiState.value.favs[i] == 0) continue

            items.add(
                suras[i].let {
                    Sura(
                        it.sura_id, "$surat ${names[it.sura_id]}",
                        it.search_name!!, it.tanzeel
                    )
                }
            )
        }

        if (searchText.isNotEmpty())
            return items.filter { it.searchName.contains(searchText, true) }
        return items
    }

    private fun getBookmarkedPageText(): String {
        val bookmarkedSura = repo.getBookmarkedSura()
        val bookmarkedPage = repo.getBookmarkedPage()

        return if (bookmarkedPage == -1) repo.getNoBookmarkedPageStr()
        else {
            "${repo.getBookmarkedPageStr()} " +
                    "${repo.getPageStr()} " +
                    "${translateNums(
                        repo.getNumeralsLanguage(), bookmarkedPage.toString()
                    )}, " +
                    "${repo.getSuraStr()} ${repo.getSuraNames()[bookmarkedSura]}"
        }
    }

    fun onSuraClick(suraId: Int, nc: NavController) {
        nc.navigate(
            Screen.QuranViewer(
                "by_surah",
                suraId = suraId.toString()
            ).route
        )
    }

    fun onQuranSearcherClick(navController: NavController) {
        navController.navigate(Screen.QuranSearcher.route)
    }

    fun onBookmarkedPageClick(navController: NavController) {
        val bookmarkedPage = repo.getBookmarkedPage()
        if (bookmarkedPage != -1) {
            navController.navigate(
                Screen.QuranViewer(
                    "by_page",
                    page = bookmarkedPage.toString()
                ).route
            )
        }
    }

    fun onPageChange(page: Int, currentPage: Int) {
        if (page != currentPage) return

        listType = ListType.values()[page]

        _uiState.update { it.copy(
            items = getItems(listType)
        )}
    }

    fun onFavClick(itemId: Int) {
        val mutableFavs = _uiState.value.favs.toMutableList()
        mutableFavs[itemId] = if (mutableFavs[itemId] == 1) 0 else 1

        _uiState.update { it.copy(
            favs = mutableFavs
        )}

        repo.updateFavorites(mutableFavs.toList())
    }

    fun onSearchTextChange(text: String) {
        searchText = text

        _uiState.update { it.copy(
            items = getItems(listType)
        )}
    }

    fun onSearchSubmit(nc: NavController) {
        try {
            val num = searchText.toInt()
            if (num in 1..604) {
                nc.navigate(
                    Screen.QuranViewer(
                        "by_page",
                        page = num.toString()
                    ).route
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
            isTutorialDialogShown = false
        )}

        if (doNotShowAgain) repo.setDoNotShowAgain()
    }

}