package bassamalim.hidaya.features.athkarList

import bassamalim.hidaya.core.enums.ListType

data class AthkarListArgs(
    val type: ListType,
    val category: Int = -1
)
