package bassamalim.hidaya.features.home.ui

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Location

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
    val language: Language = Language.ARABIC,
    val numeralsLanguage: Language = Language.ARABIC,
    val location: Location? = null
)