package bassamalim.hidaya.features.leaderboard.domain

import android.app.Application
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.models.Response
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.utils.OS.getDeviceId
import bassamalim.hidaya.features.leaderboard.ui.RankType
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LeaderboardDomain @Inject constructor(
    app: Application,
    private val userRepository: UserRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    private val deviceId = getDeviceId(app)

    suspend fun getUserRecord() = userRepository.getRemoteRecord(deviceId).first()

    suspend fun getRanks() = mapOf(
        RankType.BY_READING to userRepository.getRanks("reading_record"),
        RankType.BY_LISTENING to userRepository.getRanks("listening_record")
    )

    fun getUserRank(
        userRecord: UserRecord,
        ranks: Map<RankType, Response<List<UserRecord>>>
    ): Map<RankType, Int?> {
        return mapOf(
            RankType.BY_READING to ranks[RankType.BY_READING].let { response ->
                response?.data?.indexOfFirst { it.userId == userRecord.userId }
            },
            RankType.BY_LISTENING to ranks[RankType.BY_LISTENING].let { response ->
                response?.data?.indexOfFirst { it.userId == userRecord.userId }
            }
        )
    }

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

}