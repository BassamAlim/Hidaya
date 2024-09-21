package bassamalim.hidaya.features.remembrances.remembrancesMenu.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.remembrances.remembrancesMenu.RemembrancesItem
import bassamalim.hidaya.features.remembrances.remembrancesMenu.domain.RemembrancesMenuDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemembrancesMenuViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: RemembrancesMenuDomain,
    private val navigator: Navigator
): ViewModel() {

    private val menuType = MenuType.valueOf(savedStateHandle["type"] ?: MenuType.ALL.name)
    private val categoryId = savedStateHandle["category_id"] ?: 0

    private val _uiState = MutableStateFlow(RemembrancesMenuUiState())
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getLanguage()
    ) { state, language ->
        state.copy(
            categoryTitle =
                if (menuType == MenuType.CUSTOM) domain.getCategoryTitle(categoryId, language)
                else "",
            remembrances = domain.getRemembrances(menuType, categoryId, language).first().let { remembrances ->
                getItems(remembrances, language)
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = RemembrancesMenuUiState()
    )

    private suspend fun getItems(remembrances: List<RemembrancesItem>, language: Language) =
        remembrances.filter {
            !(language == Language.ENGLISH && !hasEn(it.id))
                    && !(_uiState.value.searchText.isNotEmpty()
                    && !it.name.contains(_uiState.value.searchText, true))
        }.map {
            RemembrancesItem(
                id = it.id,
                categoryId = it.categoryId,
                name = it.name,
                isFavorite = it.isFavorite
            )
        }

    private suspend fun hasEn(remembranceId: Int): Boolean {
        val remembrancePassages = domain.getRemembrancePassages(remembranceId)
        return remembrancePassages.any { it.textEn != null && it.textEn.length > 1 }
    }

    fun onFavoriteCLick(item: RemembrancesItem) {
        viewModelScope.launch {
            domain.setFavoriteStatus(item.id, !item.isFavorite)
        }
    }

    fun onItemClick(item: RemembrancesItem) {
        navigator.navigate(
            Screen.RemembranceReader(
                id = item.id.toString()
            )
        )
    }

    fun onSearchChange(text: String) {
        _uiState.update { it.copy(
            searchText = text
        )}
    }

}