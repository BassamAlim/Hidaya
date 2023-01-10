package bassamalim.hidaya.state

import bassamalim.hidaya.models.AthkarItem

data class AthkarListState(
    val title: String = "",
    val items: List<AthkarItem> = emptyList()
)
