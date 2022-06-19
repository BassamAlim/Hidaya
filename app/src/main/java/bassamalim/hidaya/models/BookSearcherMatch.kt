package bassamalim.hidaya.models

import android.text.Spannable

data class BookSearcherMatch(
    private val bookId: Int,
    private val bookTitle: String,
    private val chapterId: Int,
    private val chapterTitle: String,
    private val doorId: Int,
    private val doorTitle: String,
    private val text: Spannable
) {

    fun getBookId(): Int {
        return bookId
    }

    fun getBookTitle(): String {
        return bookTitle
    }

    fun getChapterId(): Int {
        return chapterId
    }

    fun getChapterTitle(): String {
        return chapterTitle
    }

    fun getDoorId(): Int {
        return doorId
    }

    fun getDoorTitle(): String {
        return doorTitle
    }

    fun getText(): Spannable {
        return text
    }
}