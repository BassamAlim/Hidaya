package bassamalim.hidaya.features.leaderboard.domain

import android.app.Application
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.utils.OS.getDeviceId
import bassamalim.hidaya.features.leaderboard.ui.RankType
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LeaderboardDomain @Inject constructor(
    app: Application,
    private val appSettingsRepository: AppSettingsRepository,
    private val userRepository: UserRepository
) {

    private val deviceId = getDeviceId(app)
    private lateinit var ranks: List<UserRecord>
    private lateinit var userRecord: UserRecord

    suspend fun fetchData(): Int {
        val userRecordResponse = userRepository.getRemoteRecord(deviceId)
        val ranksResponse = userRepository.getRanks()
        return if (userRecordResponse is Response.Success && ranksResponse is Response.Success) {
            userRecord = userRecordResponse.data!!
            ranks = ranksResponse.data!!
            userRecord.userId
        }
        else -1
    }

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    fun getUserRank(items: List<UserRecord>) =
        items.indexOfFirst { it.userId == userRecord.userId } + 1

    fun getSortedRanks(sortBy: RankType) = when (sortBy) {
        RankType.BY_READING -> ranks.sortedBy { it.quranPages }
        RankType.BY_LISTENING -> ranks.sortedBy { it.recitationsTime }
    }

}