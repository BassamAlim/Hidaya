package bassamalim.hidaya.features.quran.verseInfo.ui

import androidx.compose.ui.text.AnnotatedString

data class VerseInfoUiState(
    val isLoading: Boolean = true,
    val interpretation: AnnotatedString = AnnotatedString(""),
)