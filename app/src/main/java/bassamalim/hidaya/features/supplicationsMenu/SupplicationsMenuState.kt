package bassamalim.hidaya.features.supplicationsMenu

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.AthkarItem

data class SupplicationsMenuState(
    val listType: ListType = ListType.All,
    val items: List<AthkarItem> = emptyList(),
    val searchText: String = "",
    val language: Language = Language.ARABIC
)
