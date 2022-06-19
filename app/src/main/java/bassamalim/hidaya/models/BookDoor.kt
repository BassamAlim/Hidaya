package bassamalim.hidaya.models

data class BookDoor(
    private val doorId: Int,
    private val doorTitle: String,
    private val text: String,
    private var fav: Boolean
) {
    fun getDoorId(): Int {
        return doorId
    }

    fun getDoorTitle(): String {
        return doorTitle
    }

    fun getText(): String {
        return text
    }

    fun setFav(fav: Boolean) {
        this.fav = fav
    }

    fun isFav(): Boolean {
        return fav
    }
}