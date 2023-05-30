package bassamalim.hidaya.features.leaderboard

data class UserRecord(
    val userId: Int = -1,
    val readingRecord: Int = -1,
    val listeningRecord: Long = -1
)
