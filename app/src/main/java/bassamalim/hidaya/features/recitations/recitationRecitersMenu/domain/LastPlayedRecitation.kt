package bassamalim.hidaya.features.recitations.recitationRecitersMenu.domain

@kotlinx.serialization.Serializable
data class LastPlayedRecitation(
    val mediaId: String = "",
    val progress: Long = 0L
)