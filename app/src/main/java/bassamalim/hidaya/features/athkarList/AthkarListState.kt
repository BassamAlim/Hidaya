package bassamalim.hidaya.features.athkarList

import bassamalim.hidaya.core.models.AthkarItem

data class AthkarListState(
    val title: String = "",
    val items: List<AthkarItem> = emptyList(),
    val searchText: String = "",
)
