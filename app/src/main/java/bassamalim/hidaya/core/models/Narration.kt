package bassamalim.hidaya.core.models

data class Narration(
    val id: Int,
    val reciterId: Int,
    val name: String,
    val server: String,
    val availableSuras: String
)