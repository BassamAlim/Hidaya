package bassamalim.hidaya.features.leaderboard.domain

import android.app.Application
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.models.Response
import bassamalim.hidaya.core.models.UserRecord
import bassamalim.hidaya.core.utils.OsUtils.getDeviceId
import bassamalim.hidaya.features.leaderboard.ui.RankType
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LeaderboardDomain @Inject constructor(
    app: Application,
    private val userRepository: UserRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    private val deviceId = getDeviceId(app)
    private val previousLastDocuments: MutableMap<RankType, DocumentSnapshot?> = mutableMapOf(
        RankType.BY_READING to null,
        RankType.BY_LISTENING to null
    )

    fun getUserRank(
        userRecord: UserRecord,
        ranks: Map<RankType, Response<Map<Int, Long>>>
    ): Map<RankType, Int?> {
        return mapOf(
            RankType.BY_READING to ranks[RankType.BY_READING].let { response ->
                response?.data?.keys!!.indexOfFirst { userId -> userId == userRecord.userId }
            },
            RankType.BY_LISTENING to ranks[RankType.BY_LISTENING].let { response ->
                response?.data?.keys!!.indexOfFirst { userId -> userId == userRecord.userId }
            }
        )
    }

    suspend fun getUserRecord() = userRepository.getRemoteRecord(deviceId)?.first()

    suspend fun getRanks(): Map<RankType, Response<Map<Int, Long>>>? {
        val rawReadingRanks = userRepository.getReadingRanks()
        val rawListeningRanks = userRepository.getListeningRanks()

        if (rawReadingRanks == null || rawListeningRanks == null) return null

        val (readingRanks, lastReading) = rawReadingRanks
        val (listeningRanks, lastListening) = rawListeningRanks

        previousLastDocuments[RankType.BY_READING] = lastReading
        previousLastDocuments[RankType.BY_LISTENING] = lastListening

        return mapOf(
            RankType.BY_READING to readingRanks,
            RankType.BY_LISTENING to listeningRanks
        )
    }

    suspend fun getMoreRanks(rankType: RankType): Response<Map<Int, Long>>? {
        val rawRanks =  when (rankType) {
            RankType.BY_READING -> {
                userRepository.getReadingRanks(previousLastDocuments[rankType])
            }
            RankType.BY_LISTENING -> {
                userRepository.getListeningRanks(previousLastDocuments[rankType])
            }
        }
        if (rawRanks == null) return null
        val (ranks, last) = rawRanks

        previousLastDocuments[rankType] = last

        return ranks
    }

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

}