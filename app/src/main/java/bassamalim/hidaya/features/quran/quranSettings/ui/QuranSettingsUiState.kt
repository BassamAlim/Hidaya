package bassamalim.hidaya.features.quran.quranSettings.ui

import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.features.quranReader.ui.QuranViewType

data class QuranSettingsUiState(
    val viewType: QuranViewType = QuranViewType.PAGE,
    val textSize: Float = 20f,
    val reciterId: Int = 0,
    val repeatMode: VerseRepeatMode = VerseRepeatMode.NONE,
    val shouldStopOnSuraEnd: Boolean = false,
    val shouldStopOnPageEnd: Boolean = false
)
