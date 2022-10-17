package bassamalim.hidaya.models

import androidx.compose.ui.text.AnnotatedString

class QuranSearcherMatch(
    var id: Int,
    val ayaNum: Int,
    val suraName: String,
    var pageNum: Int,
    var text: AnnotatedString,
    val tafseer: String
)