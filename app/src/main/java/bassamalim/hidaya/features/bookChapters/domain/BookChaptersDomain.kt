package bassamalim.hidaya.features.bookChapters.domain

import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.models.Book
import javax.inject.Inject

class BookChaptersDomain @Inject constructor(
    private val booksRepo: BooksRepository,
) {

    fun getBook(bookId: Int) = booksRepo.getBook(bookId)

    fun getFavs(book: Book) = booksRepo.getChapterFavorites(book)

    suspend fun setFavs(bookId: Int, favs: Map<Int, Int>) {
        booksRepo.setChapterFavorites(bookId, favs)
    }

    fun getItems(
        listType: ListType,
        chapters: Array<Book.BookChapter>,
        favs: Map<Int, Int>,
        searchText: String
    ): List<BookChapter> {
        val items = mutableListOf<BookChapter>()
        for (i in chapters.indices) {
            val chapter = chapters[i]
            if (listType == ListType.ALL ||
                listType == ListType.FAVORITES && favs[i] == 1)
                items.add(BookChapter(chapter.chapterId, chapter.chapterTitle))
        }

        return if (searchText.isEmpty()) items
        else items.filter {
            it.title.contains(searchText, true)
        }
    }

}