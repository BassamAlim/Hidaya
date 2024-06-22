package bassamalim.hidaya.features.home

import bassamalim.hidaya.core.enums.Language

data class HomeState(
    val upcomingPrayerName: String = "",
    val upcomingPrayerTime: String = "",
    val remainingTime: String = "",
    val telawatRecord: String = "",
    val quranPagesRecord: String = "",
    val todayWerdPage: String = "25",
    val isWerdDone: Boolean = false,
    val leaderboardEnabled: Boolean = false,
    val numeralsLanguage: Language = Language.ARABIC,
)