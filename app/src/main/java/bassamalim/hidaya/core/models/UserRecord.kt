package bassamalim.hidaya.core.models

import kotlinx.serialization.Serializable

@Serializable
data class UserRecord(
    val userId: Int = -1,
    val quranPages: Int = 0,
    val recitationsTime: Long = 0L
)
