package bassamalim.hidaya.features.quran.reader.ui

import androidx.compose.ui.text.AnnotatedString

data class VersesSection(
    val annotatedString: AnnotatedString,
    val numOfLines: Int = 0
): Section()