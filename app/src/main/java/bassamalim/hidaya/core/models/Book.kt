package bassamalim.hidaya.core.models

data class Book(val info: BookInfo, val chapters: Array<BookChapter>) {

    data class BookInfo(val id: Int, val title: String, val author: String)

    data class BookChapter(
        val id: Int, val title: String, val doors: Array<BookDoor>
    ) {
        class BookDoor(val id: Int, val title: String, val text: String)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BookChapter

            if (id != other.id) return false
            if (title != other.title) return false
            if (!doors.contentEquals(other.doors)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + title.hashCode()
            result = 31 * result + doors.contentHashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Book

        if (info != other.info) return false
        if (!chapters.contentEquals(other.chapters)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = info.hashCode()
        result = 31 * result + chapters.contentHashCode()
        return result
    }

}