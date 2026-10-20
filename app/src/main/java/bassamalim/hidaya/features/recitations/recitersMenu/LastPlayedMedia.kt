package bassamalim.hidaya.features.recitations.recitersMenu

import kotlinx.serialization.Serializable

@Serializable
data class LastPlayedMedia(
    val mediaId: String = "",
    val progress: Long = 0L,
    /** The user closed the Quran menu's offer to continue it. Playing anything saves anew. */
    val isDismissed: Boolean = false
)