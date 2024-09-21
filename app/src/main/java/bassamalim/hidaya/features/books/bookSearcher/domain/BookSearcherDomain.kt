package bassamalim.hidaya.features.books.bookSearcher.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.features.books.bookSearcher.ui.BookSearcherMatch
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
        highlightColor: Color
    ): List<BookSearcherMatch> {
        val matches = mutableListOf<BookSearcherMatch>()

        val bookContents = booksRepository.getBookContents()
        for (i in bookContents.indices) {
            if (!bookSelections[i]!! || !booksRepository.isDownloaded(i))
                continue

            val bookContent = bookContents[i]
            for (j in bookContent.chapters.indices) {
                val chapter = bookContent.chapters[j]

                for (k in chapter.doors.indices) {
                    val door = chapter.doors[k]
                    val doorText = door.text

                    val matcher = Pattern.compile(searchText).matcher(doorText)
                    if (matcher.find()) {
                        val annotatedString = buildAnnotatedString {
                            append(doorText)

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
                                bookId = i,
                                bookTitle = bookContent.info.title,
                                chapterId = j,
                                chapterTitle = chapter.title,
                                doorId = k,
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

    fun getLanguage() = appSettingsRepository.getLanguage()

    fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage()

    fun getBookSelections() = booksRepository.getBookSelections()

    suspend fun setBookSelections(selections: Map<Int, Boolean>) {
        booksRepository.setBookSelections(selections)
    }

    fun getMaxMatches() = booksRepository.getSearchMaxMatches()

    suspend fun setMaxMatches(value: Int) {
        booksRepository.setSearchMaxMatches(value)
    }

    suspend fun getBookTitles(language: Language) = booksRepository.getBookTitles(language)

}