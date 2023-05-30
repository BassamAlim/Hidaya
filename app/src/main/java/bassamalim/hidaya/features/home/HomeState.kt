package bassamalim.hidaya.features.home

data class HomeState(
    val upcomingPrayerName: String = "",
    val upcomingPrayerTime: String = "",
    val remainingTime: String = "",
    val telawatRecord: String = "",
    val quranPagesRecord: String = "",
    val todayWerdPage: String = "25",
    val isWerdDone: Boolean = false,
    val leaderboardEnabled: Boolean = false
)