package bassamalim.hidaya.features.quran.settings.ui

import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType

data class QuranSettingsUiState(
    val isLoading: Boolean = true,
    val viewType: QuranViewType = QuranViewType.PAGE,
    val fillPage: Boolean = false,
    val isFillPageEnabled: Boolean = true,
    val isTextSizeSliderEnabled: Boolean = true,
    val textSize: Float = 20f,
    val reciterId: Int = 0,
    val repeatMode: VerseRepeatMode = VerseRepeatMode.NO_REPEAT,
    val shouldStopOnSuraEnd: Boolean = false,
    val shouldStopOnPageEnd: Boolean = false
)
