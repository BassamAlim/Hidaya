package bassamalim.hidaya.features.bookSearcher.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.BooksRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.features.bookSearcher.ui.BookSearcherMatch
import kotlinx.coroutines.flow.first
import java.util.regex.Pattern
import javax.inject.Inject

class BookSearcherDomain @Inject constructor(
    private val booksRepo: BooksRepository,
    private val appSettingsRepo: AppSettingsRepository
) {

    fun search(
        searchText: String,
        bookSelections: Map<Int, Boolean>,
        maxMatches: Int,
        highlightColor: Color
    ): ArrayList<BookSearcherMatch> {
        val matches = ArrayList<BookSearcherMatch>()

        val bookContents = booksRepo.getBookContents()
        for (i in bookContents.indices) {
            if (!bookSelections[i]!! || !booksRepo.isDownloaded(i))
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
                                bookTitle = bookContent.bookInfo.bookTitle,
                                chapterId = j,
                                chapterTitle = chapter.chapterTitle,
                                doorId = k,
                                doorTitle = door.doorTitle,
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

    suspend fun getLanguage() = appSettingsRepo.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    fun getBookSelections() = booksRepo.getBookSelections()

    suspend fun setBookSelections(selections: Map<Int, Boolean>) {
        booksRepo.setBookSelections(selections)
    }

    fun getMaxMatches() = booksRepo.getMaxMatches()

    suspend fun setMaxMatches(value: Int) {
        booksRepo.setMaxMatches(value)
    }

    fun getMaxMatchesItems() = booksRepo.getMaxMatchesItems()

    fun getBookTitles(language: Language) = booksRepo.getBookTitles(language)

}