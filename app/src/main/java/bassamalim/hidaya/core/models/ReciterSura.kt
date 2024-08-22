package bassamalim.hidaya.core.models

data class ReciterSura(
    val num: Int,
    val suraName: String,
    val searchName: String,
    val isFavorite: Int = 0
)