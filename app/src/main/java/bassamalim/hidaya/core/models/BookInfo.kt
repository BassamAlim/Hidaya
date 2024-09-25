package bassamalim.hidaya.core.models

data class BookInfo(
    val id: Int,
    val title: String,
    val author: String,
    val url: String,
    val isFavorite: Boolean
)