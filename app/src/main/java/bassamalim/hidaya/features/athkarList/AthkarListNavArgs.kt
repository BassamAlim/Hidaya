package bassamalim.hidaya.features.athkarList

import bassamalim.hidaya.core.enums.ListType

data class AthkarListNavArgs(
    val type: ListType,
    val category: Int = -1
)
