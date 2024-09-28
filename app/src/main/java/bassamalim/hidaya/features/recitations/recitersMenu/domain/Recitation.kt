package bassamalim.hidaya.features.recitations.recitersMenu.domain

import bassamalim.hidaya.core.enums.DownloadState
import java.io.Serializable

data class Recitation(
    val reciterId: Int,
    val reciterName: String,
    val isFavoriteReciter: Boolean,
    val narrations: List<Narration>
) {
    data class Narration(
        val id: Int,
        val name: String,
        val server: String,
        val availableSuras: String,
        val downloadState: DownloadState
    ) : Serializable
}