package bassamalim.hidaya.features.supplicationsMenu

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType

data class SupplicationsMenuState(
    val listType: ListType = ListType.ALL,
    val items: List<AthkarItem> = emptyList(),
    val searchText: String = "",
    val language: Language = Language.ARABIC
)
