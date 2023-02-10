package bassamalim.hidaya.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import bassamalim.hidaya.database.dbs.AthkarDB
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.AthkarItem
import bassamalim.hidaya.repository.AthkarListRepo
import bassamalim.hidaya.state.AthkarListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AthkarListVM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: AthkarListRepo
): ViewModel() {

    private val type = savedStateHandle.get<String>("type") ?: "all"
    private val category = savedStateHandle.get<Int>("category")?: 0

    var searchText by mutableStateOf("")
        private set
    private val language = repo.getLanguage()

    private val _uiState = MutableStateFlow(AthkarListState(
        title = when (type) {
            ListType.Favorite.name -> repo.getFavoriteAthkarStr()
            ListType.Custom.name -> repo.getName(language, category)
            else -> repo.getAllAthkarStr()
        },
        items = getItems()
    ))
    val uiState = _uiState.asStateFlow()

    private fun getItems(): List<AthkarItem> {
        val athkar = repo.getAthkar(type, category)
        val items = ArrayList<AthkarItem>()

        for (i in athkar.indices) {
            val thikr = athkar[i]

            if (language == Language.ENGLISH && !hasEn(thikr)) continue

            val name =
                if (language == Language.ENGLISH) thikr.name_en!!
                else thikr.name!!

            items.add(
                AthkarItem(
                    thikr.id, thikr.category_id, name, mutableStateOf(thikr.favorite)
                )
            )
        }

        return if (searchText.isNotEmpty())
            items.filter { it.name.contains(searchText, true) }
        else items
    }

    private fun hasEn(thikr: AthkarDB): Boolean {
        val ts = repo.getThikrs(thikr.id)

        for (i in ts.indices) {
            val t = ts[i]
            if (t.getTextEn() != null && t.getTextEn()!!.length > 1) return true
        }
        return false
    }

    fun onFavoriteCLick(item: AthkarItem) {
        if (item.favorite.value == 0) {
            repo.setFavorite(item.id, 1)
            item.favorite.value = 1
        }
        else {
            repo.setFavorite(item.id, 0)
            item.favorite.value = 0
        }

        repo.updateFavorites()
    }

    fun onItemClick(navController: NavController, item: AthkarItem) {
        // pass type and thikrId which is item.id
        navController.navigate(
            Screen.AthkarViewer(
                item.id.toString()
            ).route
        )
    }

    fun onSearchChange(text: String) {
        searchText = text

        _uiState.update { it.copy(
            items = getItems()
        )}
    }

}