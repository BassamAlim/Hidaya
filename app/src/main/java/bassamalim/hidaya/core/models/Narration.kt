package bassamalim.hidaya.core.models

data class Narration(
    val id: Int,
    val reciterId: Int,
    val name: String,
    val server: String,
    val availableSuras: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Narration

        if (id != other.id) return false
        if (reciterId != other.reciterId) return false
        if (name != other.name) return false
        if (server != other.server) return false
        if (!availableSuras.contentEquals(other.availableSuras)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + reciterId
        result = 31 * result + name.hashCode()
        result = 31 * result + server.hashCode()
        result = 31 * result + availableSuras.contentHashCode()
        return result
    }
}