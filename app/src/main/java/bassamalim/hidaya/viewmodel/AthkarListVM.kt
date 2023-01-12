package bassamalim.hidaya.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.Screen
import bassamalim.hidaya.database.dbs.AthkarDB
import bassamalim.hidaya.enum.Language
import bassamalim.hidaya.enum.ListType
import bassamalim.hidaya.models.AthkarItem
import bassamalim.hidaya.repository.AthkarListRepo
import bassamalim.hidaya.state.AthkarListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AthkarListVM @Inject constructor(
    app: Application,
    savedStateHandle: SavedStateHandle,
    private val repository: AthkarListRepo
): AndroidViewModel(app) {

    private val type = savedStateHandle.get<String>("type") ?: "all"
    private val category = savedStateHandle.get<Int>("category")?: 0

    private val context = getApplication<Application>().applicationContext
    var searchText by mutableStateOf("")
        private set
    private val language = repository.getLanguage()

    private val _uiState = MutableStateFlow(AthkarListState(
        title = when (ListType.valueOf(type)) {
            ListType.Favorite -> context.getString(R.string.favorite_athkar)
            ListType.Custom -> repository.getName(language, category)
            else -> context.getString(R.string.all_athkar)
        },
        items = getItems().filter { item ->
            item.name.contains(searchText, ignoreCase = true)
        }
    ))
    val uiState = _uiState.asStateFlow()

    private fun getItems(): List<AthkarItem> {
        val athkar = repository.getAthkar(type, category)
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
        return items
    }

    private fun hasEn(thikr: AthkarDB): Boolean {
        val ts = repository.getThikrs(thikr.id)

        for (i in ts.indices) {
            val t = ts[i]
            if (t.getTextEn() != null && t.getTextEn()!!.length > 1) return true
        }
        return false
    }

    fun onFavoriteCLick(item: AthkarItem) {
        if (item.favorite.value == 0) {
            repository.setFavorite(item.id, 1)
            item.favorite.value = 1
        }
        else {
            repository.setFavorite(item.id, 0)
            item.favorite.value = 0
        }

        repository.updateFavorites()
    }

    fun onItemClick(navController: NavController, item: AthkarItem) {
        // pass type and thikrId which is item.id
        navController.navigate(Screen.AthkarViewer.withArgs(item.id.toString()))
    }

}