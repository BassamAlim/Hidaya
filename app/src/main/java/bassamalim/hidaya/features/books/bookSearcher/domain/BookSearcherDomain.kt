package bassamalim.hidaya.features.books.bookSearcher.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.features.books.bookSearcher.ui.BookSearcherMatch
import kotlinx.coroutines.flow.first
import java.util.regex.Pattern
import javax.inject.Inject

class BookSearcherDomain @Inject constructor(
    private val booksRepository: BooksRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun search(
        searchText: String,
        bookSelections: Map<Int, Boolean>,
        maxMatches: Int,
        language: Language,
        highlightColor: Color
    ): List<BookSearcherMatch> {
        val matches = mutableListOf<BookSearcherMatch>()

        val bookContents = booksRepository.getBookContents(language)
        for ((bookId, bookContent) in bookContents) {
            if (!bookSelections[bookId]!! || !booksRepository.isDownloaded(bookId))
                continue

            for ((c, chapter) in bookContent.chapters.withIndex()) {
                for ((d, door) in chapter.doors.withIndex()) {
                    val matcher = Pattern.compile(searchText).matcher(door.text)
                    if (matcher.find()) {
                        val annotatedString = buildAnnotatedString {
                            append(door.text)

                            do {
                                addStyle(
                                    style = SpanStyle(color = highlightColor),
                                    start = matcher.start(),
                                    end = matcher.end()
                                )
                            } while (matcher.find())
                        }

                        matches.add(
                            BookSearcherMatch(
                                bookId = bookId,
                                bookTitle = bookContent.info.title,
                                chapterId = c,
                                chapterTitle = chapter.title,
                                doorId = d,
                                doorTitle = door.title,
                                text = annotatedString
                            )
                        )

                        if (matches.size == maxMatches)
                            return matches
                    }
                }
            }
        }

        return matches
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    fun getBookSelections() = booksRepository.getSearchSelections()

    fun getMaxMatches() = booksRepository.getSearchMaxMatches()

    suspend fun setMaxMatches(value: Int) {
        booksRepository.setSearchMaxMatches(value)
    }

    suspend fun getBookTitles(language: Language) = booksRepository.getBookTitles(language)

}