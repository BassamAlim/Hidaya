package bassamalim.hidaya.core.models

import java.io.Serializable

data class Reciter(
    val id: Int,
    val name: String,
    val narrations: List<RecitationNarration>,
    var isFavorite: Boolean = false
) {
    data class RecitationNarration(
        val id: Int,
        val name: String,
        val server: String,
        val availableSuras: String
    ) : Serializable
}