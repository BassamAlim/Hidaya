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
        val availableSuras: IntArray,
        val downloadState: DownloadState
    ) : Serializable {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Narration

            if (id != other.id) return false
            if (name != other.name) return false
            if (server != other.server) return false
            if (!availableSuras.contentEquals(other.availableSuras)) return false
            if (downloadState != other.downloadState) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + name.hashCode()
            result = 31 * result + server.hashCode()
            result = 31 * result + availableSuras.contentHashCode()
            result = 31 * result + downloadState.hashCode()
            return result
        }
    }
}