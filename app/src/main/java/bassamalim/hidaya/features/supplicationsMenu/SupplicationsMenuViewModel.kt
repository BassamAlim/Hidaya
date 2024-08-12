package bassamalim.hidaya.features.supplicationsMenu

import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.data.database.models.Remembrance
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SupplicationsMenuViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: SupplicationsMenuRepository,
    private val navigator: Navigator
): ViewModel() {

    private val type = savedStateHandle.get<String>("type") ?: ListType.ALL.name
    private val category = savedStateHandle.get<Int>("category")?: 0

    private val _uiState = MutableStateFlow(SupplicationsMenuState(
        language = repo.getLanguage(),
        listType = ListType.valueOf(type),
        items = getItems()
    ))
    val uiState = _uiState.asStateFlow()

    private fun getItems(): List<AthkarItem> {
        val athkar = repo.getAthkar(type, category)
        val items = ArrayList<AthkarItem>()

        val isEng = _uiState.value.language == Language.ENGLISH
        for (thikr in athkar) {
            if (isEng && !hasEn(thikr)) continue

            val name =
                if (isEng) thikr.name_en!!
                else thikr.name_ar!!

            items.add(
                AthkarItem(
                    id = thikr.id,
                    category_id = thikr.category_id,
                    name = name,
                    favorite = mutableIntStateOf(thikr.is_favorite)
                )
            )
        }

        return if (_uiState.value.searchText.isEmpty()) items
        else items.filter { it.name.contains(_uiState.value.searchText, true) }
    }

    private fun hasEn(thikr: Remembrance): Boolean {
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
        navigator.navigate(
            Screen.AthkarViewer(
                thikrId = item.id.toString()
            )
        )
    }

    fun onSearchChange(text: String) {
        _uiState.update { it.copy(
            items = getItems(),
            searchText = text
        )}
    }

    fun getName() = repo.getName(_uiState.value.language, category)

}