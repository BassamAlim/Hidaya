package bassamalim.hidaya.features.home.ui

import bassamalim.hidaya.core.enums.Language

data class HomeUiState(
    val upcomingPrayerName: String = "",
    val upcomingPrayerTime: String = "",
    val remaining: String = "",
    val timeFromPreviousPrayer: Long = 0L,
    val timeToNextPrayer: Long = 0L,
    val werdPage: String = "25",
    val isWerdDone: Boolean = false,
    val quranRecord: String = "",
    val recitationsRecord: String = "",
    val isLeaderboardEnabled: Boolean = false,
    val numeralsLanguage: Language = Language.ENGLISH
)