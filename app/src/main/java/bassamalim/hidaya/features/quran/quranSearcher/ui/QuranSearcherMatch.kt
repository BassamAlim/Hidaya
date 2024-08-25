package bassamalim.hidaya.features.quran.quranSearcher.ui

import androidx.compose.ui.text.AnnotatedString

class QuranSearcherMatch(
    var id: Int,
    val ayaNum: String,
    val suraName: String,
    var pageNum: String,
    var text: AnnotatedString,
    val interpretation: String
)