package bassamalim.hidaya.features.quran.surasMenu.ui

import androidx.compose.ui.text.AnnotatedString

data class QuranSearcherMatch(
    var id: Int,
    val verseNum: String,
    val suraName: String,
    var text: AnnotatedString
)