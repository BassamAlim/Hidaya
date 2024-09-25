package bassamalim.hidaya.core.models

data class Book(
    val id: Int,
    val title: String,
    val chapters: List<Chapter>
) {
    data class Chapter(
        val id: Int,
        val title: String,
        val doors: List<Door>,
        val isFavorite: Boolean
    ) {
        data class Door(
            val id: Int,
            val title: String,
            val text: String
        )
    }
}