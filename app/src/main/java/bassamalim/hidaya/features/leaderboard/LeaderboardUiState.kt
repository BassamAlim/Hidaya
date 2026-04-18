package bassamalim.hidaya.features.leaderboard

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val userId: String = "",
    val userRanks: Map<RankType, String> = emptyMap(),
    val userRankInts: Map<RankType, Int> = emptyMap(),
    val ranks: Map<RankType, List<Pair<String, String>>> = emptyMap(),
    val isLoadingItems: Map<RankType, Boolean> = mapOf(
        RankType.BY_READING to false,
        RankType.BY_LISTENING to false
    )
)