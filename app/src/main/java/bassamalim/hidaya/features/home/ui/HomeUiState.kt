package bassamalim.hidaya.features.home.ui

import bassamalim.hidaya.core.enums.Language

data class HomeUiState(
    val isLoading: Boolean = true,
    val previousPrayerName: String = "",
    val previousPrayerTime: String = "",
    val passed: String = "",
    val nextPrayerName: String = "",
    val nextPrayerTime: String = "",
    val remaining: String = "",
    val timeFromPreviousPrayer: Long = 0L,
    val timeToNextPrayer: Long = 0L,
    val werdPage: String = "",
    val isWerdDone: Boolean = false,
    val quranRecord: String = "",
    val recitationsRecord: String = "",
    val isLeaderboardEnabled: Boolean = false,
    val language: Language = Language.ARABIC,
    val numeralsLanguage: Language = Language.ARABIC
)