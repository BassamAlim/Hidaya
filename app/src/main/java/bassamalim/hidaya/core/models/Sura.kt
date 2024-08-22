package bassamalim.hidaya.core.models

data class Sura(
    val id: Int,
    val decoratedName: String,
    val plainName: String,
    val revelation: Int,
    val isFavorite: Boolean
)