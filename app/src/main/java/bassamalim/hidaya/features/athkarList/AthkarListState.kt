package bassamalim.hidaya.features.athkarList

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.AthkarItem

data class AthkarListState(
    val listType: ListType = ListType.All,
    val items: List<AthkarItem> = emptyList(),
    val searchText: String = "",
    val language: Language = Language.ARABIC
)
