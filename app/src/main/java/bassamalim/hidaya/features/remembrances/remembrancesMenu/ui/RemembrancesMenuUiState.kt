package bassamalim.hidaya.features.remembrances.remembrancesMenu.ui

import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.features.remembrances.remembrancesMenu.RemembrancesItem

data class RemembrancesMenuUiState(
    val isLoading: Boolean = true,
    val categoryTitle: String = "",
    val menuType: MenuType = MenuType.ALL,
    val remembrances: List<RemembrancesItem> = emptyList(),
    val searchText: String = ""
)