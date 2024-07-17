package bassamalim.hidaya.features.leaderboard.domain

import android.app.Application
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.utils.OS.getDeviceId
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.features.leaderboard.data.LeaderboardRepository
import bassamalim.hidaya.features.leaderboard.ui.RankType
import javax.inject.Inject

class LeaderboardDomain @Inject constructor(
    app: Application,
    private val repository: LeaderboardRepository
) {

    private val deviceId = getDeviceId(app)
    private lateinit var ranks: List<UserRecord>
    private lateinit var userRecord: UserRecord

    suspend fun fetchData(): Int {
        val userRecordResponse = repository.getUserRecord(deviceId)
        val ranksResponse = repository.getRanks()
        return if (userRecordResponse is Response.Success && ranksResponse is Response.Success) {
            userRecord = userRecordResponse.data!!
            ranks = ranksResponse.data!!
            userRecord.userId
        }
        else -1
    }

    suspend fun getNumeralsLanguage() = repository.getNumeralsLanguage()

    fun getUserRank(items: List<UserRecord>) =
        items.indexOfFirst { it.userId == userRecord.userId } + 1

    fun getSortedRanks(sortBy: RankType) = when (sortBy) {
        RankType.BY_READING -> ranks.sortedBy { it.quranPages }
        RankType.BY_LISTENING -> ranks.sortedBy { it.recitationsTime }
    }

}