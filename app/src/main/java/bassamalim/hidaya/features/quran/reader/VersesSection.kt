package bassamalim.hidaya.features.quran.reader

import androidx.compose.ui.text.AnnotatedString

data class VersesSection(
    val suraNum: Int,
    val annotatedString: AnnotatedString,
    val numOfLines: Int = 0
): Section()