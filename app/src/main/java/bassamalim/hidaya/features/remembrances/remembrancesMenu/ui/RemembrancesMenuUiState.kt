package bassamalim.hidaya.features.remembrances.remembrancesMenu.ui

import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.features.remembrances.remembrancesMenu.RemembrancesItem

data class RemembrancesMenuUiState(
    val categoryTitle: String = "",
    val listType: ListType = ListType.ALL,
    val remembrances: List<RemembrancesItem> = emptyList(),
    val searchText: String = "",
)