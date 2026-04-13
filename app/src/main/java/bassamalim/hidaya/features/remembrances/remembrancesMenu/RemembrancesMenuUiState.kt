package bassamalim.hidaya.features.remembrances.remembrancesMenu

import bassamalim.hidaya.core.enums.MenuType

data class RemembrancesMenuUiState(
    val isLoading: Boolean = true,
    val categoryTitle: String = "",
    val menuType: MenuType = MenuType.ALL,
    val remembrances: List<RemembrancesItem> = emptyList(),
    val searchText: String = ""
)