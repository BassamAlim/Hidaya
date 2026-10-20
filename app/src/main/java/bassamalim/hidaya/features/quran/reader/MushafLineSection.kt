package bassamalim.hidaya.features.quran.reader

import androidx.compose.ui.text.AnnotatedString

/** One printed mushaf line, its words in reading order, each tagged with its verse id. */
data class MushafLineSection(val words: List<AnnotatedString>): Section()
