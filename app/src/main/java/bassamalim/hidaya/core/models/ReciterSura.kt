package bassamalim.hidaya.core.models

data class ReciterSura(
    val id: Int,
    val suraName: String,
    val searchName: String,
    val isFavorite: Boolean = false
)