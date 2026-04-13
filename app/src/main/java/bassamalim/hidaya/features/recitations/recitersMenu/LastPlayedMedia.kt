package bassamalim.hidaya.features.recitations.recitersMenu

import kotlinx.serialization.Serializable

@Serializable
data class LastPlayedMedia(
    val mediaId: String = "",
    val progress: Long = 0L
)