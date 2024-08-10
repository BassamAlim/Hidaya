package bassamalim.hidaya.core.models

import kotlinx.serialization.Serializable

@Serializable
data class QuranPageBookmark(
    val pageNum: Int,
    val suraId: Int
)
