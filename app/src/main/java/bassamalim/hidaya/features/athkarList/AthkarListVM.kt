package bassamalim.hidaya.features.athkarList

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.data.database.dbs.AthkarDB
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.AthkarItem
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AthkarListVM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: AthkarListRepo,
    private val navigator: Navigator
): ViewModel() {

    private val type = savedStateHandle.get<String>("type") ?: ListType.All.name
    private val category = savedStateHandle.get<Int>("category")?: 0

    var searchText by mutableStateOf("")
        private set
    private val language = repo.getLanguage()

    private val _uiState = MutableStateFlow(
        AthkarListState(
            title = when (type) {
                ListType.Favorite.name -> repo.getFavoriteAthkarStr()
                ListType.Custom.name -> repo.getName(language, category)
                else -> repo.getAllAthkarStr()
            },
            items = getItems()
        )
    )
    val uiState = _uiState.asStateFlow()

    private fun getItems(): List<AthkarItem> {
        val athkar = repo.getAthkar(type, category)
        val items = ArrayList<AthkarItem>()

        val isEng = language == Language.ENGLISH
        for (thikr in athkar) {
            if (isEng && !hasEn(thikr)) continue

            val name =
                if (isEng) thikr.name_en!!
                else thikr.name!!

            items.add(
                AthkarItem(
                    thikr.id, thikr.category_id, name, mutableStateOf(thikr.favorite)
                )
            )
        }

        return if (searchText.isEmpty()) items
        else items.filter { it.name.contains(searchText, true) }
    }

    private fun hasEn(thikr: AthkarDB): Boolean {
        val thikrParts = repo.getThikrParts(thikr.id)

        for (i in thikrParts.indices) {
            val t = thikrParts[i]
            if (t.textEn != null && t.textEn.length > 1) return true
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

    fun onItemClick(item: AthkarItem) {
        // pass type and thikrId which is item.id
        navigator.navigate(
            Screen.AthkarViewer(
                item.id.toString()
            )
        )
    }

    fun onSearchChange(text: String) {
        searchText = text

        _uiState.update { it.copy(
            items = getItems()
        )}
    }

}