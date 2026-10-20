package bassamalim.hidaya.features.quran.reader

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Verse
import bassamalim.hidaya.core.data.dataSources.room.entities.Verse as VerseEntity

/**
 * Turns a mushaf page's verses into the sections the reader draws: sura headers, basmalahs and
 * the verses, each verse tagged with its id so taps can find it.
 */
class QuranPageBuilder(
    private val allVerses: List<VerseEntity>,
    private val suraNames: List<String>,
    private val language: Language
) {

    fun getPageVerses(pageNumber: Int) =
        allVerses.filter { it.pageNum == pageNumber }.map(::toModel)

    /** Page view: the verses between sura starts run together as one text block. */
    fun buildPage(
        pageNumber: Int,
        selectedVerseId: Int?,
        trackedVerseId: Int,
        defaultVerseColor: Color,
        selectedVerseColor: Color,
        trackedVerseColor: Color
    ): List<Section> {
        val sections = mutableListOf<Section>()
        val pendingVerses = mutableListOf<Verse>()

        fun flushVerses() {
            if (pendingVerses.isEmpty()) return

            sections.add(
                VersesSection(
                    suraNum = pendingVerses.last().suraNum,
                    annotatedString = versesToAnnotatedString(
                        verses = pendingVerses,
                        selectedVerseId = selectedVerseId,
                        trackedVerseId = trackedVerseId,
                        defaultVerseColor = defaultVerseColor,
                        selectedVerseColor = selectedVerseColor,
                        trackedVerseColor = trackedVerseColor
                    ),
                    numOfLines =
                        pendingVerses.last().endLineNum - pendingVerses.first().startLineNum + 1
                )
            )
            pendingVerses.clear()
        }

        for (verse in pageVerseEntities(pageNumber)) {
            if (verse.num == 1) {
                flushVerses()
                addSuraStart(sections, verse)
            }
            pendingVerses.add(toModel(verse))
        }
        flushVerses()

        return sections
    }

    /** List view: each verse is its own row, with its translation. */
    fun buildListPage(
        pageNumber: Int,
        selectedVerseId: Int?,
        trackedVerseId: Int,
        defaultVerseColor: Color,
        selectedVerseColor: Color,
        trackedVerseColor: Color
    ): List<Section> {
        val sections = mutableListOf<Section>()

        for (verse in pageVerseEntities(pageNumber)) {
            if (verse.num == 1) addSuraStart(sections, verse)

            sections.add(
                ListVerse(
                    id = verse.id,
                    text = versesToAnnotatedString(
                        verses = listOf(toModel(verse)),
                        selectedVerseId = selectedVerseId,
                        trackedVerseId = trackedVerseId,
                        defaultVerseColor = defaultVerseColor,
                        selectedVerseColor = selectedVerseColor,
                        trackedVerseColor = trackedVerseColor
                    ),
                    translation = verse.translationEn
                )
            )
        }

        return sections
    }

    /**
     * Mushaf view: the page's lines as printed, null for a blank one. Trailing blanks are dropped,
     * so the two opening pages come out shorter.
     */
    fun buildMushafPage(
        pageNumber: Int,
        selectedVerseId: Int?,
        trackedVerseId: Int,
        defaultVerseColor: Color,
        selectedVerseColor: Color,
        trackedVerseColor: Color
    ): List<Section?> {
        val lines = arrayOfNulls<Section>(MUSHAF_LINES)
        val lineWords = sortedMapOf<Int, MutableList<AnnotatedString>>()
        val pageVerses = pageVerseEntities(pageNumber)

        for (verse in pageVerses) {
            if (verse.num == 1) {
                for ((line, section) in suraStartLines(verse))
                    if (line >= 1) lines[line - 1] = section
            }

            val color = when (verse.id) {
                selectedVerseId -> selectedVerseColor
                trackedVerseId -> trackedVerseColor
                else -> defaultVerseColor
            }
            verse.mushafText.split('\n').forEachIndexed { i, segment ->
                segment.split(' ').mapTo(lineWords.getOrPut(verse.startLineNum + i) { mutableListOf() }) {
                    buildAnnotatedString {
                        pushStringAnnotation(tag = verse.id.toString(), annotation = verse.id.toString())
                        withStyle(SpanStyle(color = color)) { append(it) }
                        pop()
                    }
                }
            }
        }

        // A sura opening the next page can have its header at the foot of this one
        allVerses.firstOrNull { it.pageNum == pageNumber + 1 }
            ?.takeIf { it.num == 1 }
            ?.let { next ->
                for ((line, section) in suraStartLines(next))
                    if (line < 1) lines[MUSHAF_LINES + line - 1] = section
            }

        for ((line, words) in lineWords) lines[line - 1] = MushafLineSection(words)

        return lines.toList().dropLastWhile { it == null }
    }

    /**
     * The header and basmalah lines just above a sura's first verse, numbered on that verse's page,
     * so a line below 1 falls at the end of the page before.
     */
    private fun suraStartLines(firstVerse: VerseEntity): List<Pair<Int, Section>> {
        val header = SuraHeaderSection(
            suraNum = firstVerse.suraNum,
            suraName = suraNames[firstVerse.suraNum - 1]
        )
        // Al-Fatiha's basmalah is its first verse, and At-Tawbah has none
        return if (firstVerse.suraNum == 1 || firstVerse.suraNum == 9)
            listOf(firstVerse.startLineNum - 1 to header)
        else listOf(
            firstVerse.startLineNum - 2 to header,
            firstVerse.startLineNum - 1 to BasmalahSection()
        )
    }

    /** The page's verses, which are contiguous since verses are ordered by page. */
    private fun pageVerseEntities(pageNumber: Int): List<VerseEntity> {
        val start = allVerses.indexOfFirst { it.pageNum == pageNumber }
        if (start == -1) return emptyList()

        var end = start
        while (end + 1 < allVerses.size && allVerses[end + 1].pageNum == pageNumber) end++
        return allVerses.subList(start, end + 1)
    }

    private fun addSuraStart(sections: MutableList<Section>, firstVerse: VerseEntity) {
        sections.add(
            SuraHeaderSection(
                suraNum = firstVerse.suraNum,
                suraName = suraNames[firstVerse.suraNum - 1]
            )
        )

        // Al-Fatiha's basmalah is its first verse, and At-Tawbah has none
        if (firstVerse.suraNum != 1 && firstVerse.suraNum != 9)
            sections.add(BasmalahSection())
    }

    private fun toModel(verse: VerseEntity) = Verse(
        id = verse.id,
        juzNum = verse.juzNum,
        suraNum = verse.suraNum,
        suraName = suraNames[verse.suraNum - 1],
        num = verse.num,
        text = "${verse.decoratedText} ",
        startLineNum = verse.startLineNum,
        endLineNum = verse.endLineNum,
        translation = verse.translationEn,
        interpretation = verse.interpretation
    )

    private fun versesToAnnotatedString(
        verses: List<Verse>,
        selectedVerseId: Int?,
        trackedVerseId: Int,
        defaultVerseColor: Color,
        selectedVerseColor: Color,
        trackedVerseColor: Color
    ): AnnotatedString {
        return buildAnnotatedString {
            for (verse in verses) {
                val text = verse.text.orEmpty()
                val color = when (verse.id) {
                    selectedVerseId -> selectedVerseColor
                    trackedVerseId -> trackedVerseColor
                    else -> defaultVerseColor
                }

                pushStringAnnotation(tag = verse.id.toString(), annotation = verse.id.toString())
                withStyle(style = SpanStyle(color = color)) {
                    append(
                        when (language) {
                            Language.ARABIC -> text
                            Language.ENGLISH -> text.reversed()
                        }
                    )
                }
                pop()
            }
        }
    }

    companion object {
        const val MUSHAF_LINES = 15
    }

}
