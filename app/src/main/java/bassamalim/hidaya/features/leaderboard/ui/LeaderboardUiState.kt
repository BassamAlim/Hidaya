package bassamalim.hidaya.features.leaderboard.ui

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val userId: String = "",
)