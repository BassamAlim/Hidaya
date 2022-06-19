package bassamalim.hidaya.models

data class Book(val bookInfo: BookInfo, val chapters: Array<BookChapter>) {

    class BookInfo(val bookId: Int, val bookTitle: String, val author: String)
    class BookChapter(
        val chapterId: Int,
        val chapterTitle: String,
        val doors: Array<BookDoor>
    ) {
        class BookDoor(val doorId: Int, val doorTitle: String, val text: String)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Book

        if (bookInfo != other.bookInfo) return false
        if (!chapters.contentEquals(other.chapters)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bookInfo.hashCode()
        result = 31 * result + chapters.contentHashCode()
        return result
    }
}