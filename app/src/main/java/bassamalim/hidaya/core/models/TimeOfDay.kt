package bassamalim.hidaya.core.models

import kotlinx.serialization.Serializable

@Serializable
data class TimeOfDay(
    val hour: Int,
    val minute: Int
)
