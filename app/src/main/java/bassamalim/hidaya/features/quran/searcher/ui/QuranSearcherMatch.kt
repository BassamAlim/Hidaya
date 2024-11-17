package bassamalim.hidaya.features.quran.searcher.ui

import androidx.compose.ui.text.AnnotatedString

data class QuranSearcherMatch(
    var id: Int,
    val verseNum: String,
    val suraName: String,
    var pageNum: String,
    var text: AnnotatedString,
    val interpretation: AnnotatedString
)