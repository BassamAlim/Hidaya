package bassamalim.hidaya.features.leaderboard

data class LeaderboardState(
    val loading: Boolean = true,
    val userId: String = "",
    val userPosition: String = ""
)