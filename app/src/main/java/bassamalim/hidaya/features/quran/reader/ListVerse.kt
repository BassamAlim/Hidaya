package bassamalim.hidaya.features.quran.reader

import androidx.compose.ui.text.AnnotatedString

data class ListVerse(
    val id: Int,
    val text: AnnotatedString,
    val translation: String?
): Section()
