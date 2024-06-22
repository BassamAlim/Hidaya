package bassamalim.hidaya.features.bookChapters.domain

import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.features.bookChapters.data.BookChaptersRepository
import javax.inject.Inject

class BookChaptersDomain @Inject constructor(
    private val repository: BookChaptersRepository
) {

    fun getBook(bookId: Int) = repository.getBook(bookId)

    suspend fun getFavs(book: Book) = repository.getFavs(book)

    suspend fun setFavs(bookId: Int, favs: Map<Int, Int>) = repository.setFavs(bookId, favs)

    fun getItems(
        listType: ListType,
        chapters: Array<Book.BookChapter>,
        favs: Map<Int, Int>,
        searchText: String
    ): List<BookChapter> {
        val items = ArrayList<BookChapter>()
        for (i in chapters.indices) {
            val chapter = chapters[i]
            if (listType == ListType.All ||
                listType == ListType.Favorite && favs[i] == 1)
                items.add(BookChapter(chapter.chapterId, chapter.chapterTitle))
        }

        return if (searchText.isEmpty()) items
        else items.filter {
            it.title.contains(searchText, true)
        }
    }

}