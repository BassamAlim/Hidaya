package bassamalim.hidaya.features.recitations.recitersMenu.domain

@kotlinx.serialization.Serializable
data class LastPlayedMedia(
    val mediaId: String = "",
    val progress: Long = 0L
)