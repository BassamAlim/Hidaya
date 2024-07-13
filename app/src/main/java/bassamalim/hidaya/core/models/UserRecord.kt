package bassamalim.hidaya.core.models

import kotlinx.serialization.Serializable

@Serializable
data class UserRecord(
    val quranPages: Int = 0,
    val recitationsTime: Long = 0L
)
