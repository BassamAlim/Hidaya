package bassamalim.hidaya.features.remembrances.remembrancesMenu.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.remembrances.remembrancesMenu.RemembrancesItem
import bassamalim.hidaya.features.remembrances.remembrancesMenu.domain.RemembrancesMenuDomain
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
class RemembrancesMenuViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: RemembrancesMenuDomain,
    private val navigator: Navigator
): ViewModel() {

    private val type = savedStateHandle.get<String>("type") ?: ListType.ALL.name
    private val category = savedStateHandle.get<Int>("category")?: 0

    private var language: Language? = null

    private val _uiState = MutableStateFlow(RemembrancesMenuUiState(
        listType = ListType.valueOf(type),
    ))
    val uiState = combine(
        _uiState.asStateFlow(),
        domain.getRemembrances(type, category, language)
    ) { state, remembrances ->
        state.copy(
            remembrances = getItems(remembrances)
        )
    }.stateIn(
        initialValue = RemembrancesMenuUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000)
    )

    init {
        viewModelScope.launch {
            language = domain.getLanguage()

            _uiState.update { it.copy(
                categoryTitle = domain.getCategoryTitle(category, language!!)
            )}
        }
    }

    private fun getItems(remembrances: List<RemembrancesItem>) =
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

    private fun hasEn(remembranceId: Int): Boolean {
        val remembrancePassages = domain.getRemembrancePassages(remembranceId)
        return remembrancePassages.any { it.textEn != null && it.textEn.length > 1 }
    }

    fun onFavoriteCLick(item: RemembrancesItem) {
        viewModelScope.launch {
            domain.setIsFavorite(item.id, !item.isFavorite)
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