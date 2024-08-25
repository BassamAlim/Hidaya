package bassamalim.hidaya.features.recitationRecitersMenu.domain

@kotlinx.serialization.Serializable
data class LastPlayedRecitation(
    val mediaId: String = "",
    val progress: Long = 0L
)