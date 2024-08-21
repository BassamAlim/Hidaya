package bassamalim.hidaya.core.models

import java.io.Serializable

data class Recitation(
    val reciterId: Int,
    val reciterName: String,
    val reciterIsFavorite: Boolean,
    val narrations: List<Narration>
) {
    data class Narration(
        val id: Int,
        val name: String,
        val server: String,
        val availableSuras: String
    ) : Serializable
}
