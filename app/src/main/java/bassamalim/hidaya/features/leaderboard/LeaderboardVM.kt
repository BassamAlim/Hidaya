package bassamalim.hidaya.features.leaderboard

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.data.Response
import bassamalim.hidaya.core.utils.LangUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class LeaderboardVM @Inject constructor(
    app: Application,
    private val repo: LeaderboardRepo
): AndroidViewModel(app) {

    private val deviceId = getDeviceId(app)
    private var remoteUserRecord: UserRecord? = null
    private var items = mutableListOf<LeaderboardItem>()

    private val _uiState = MutableStateFlow(LeaderboardState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (
                val response = repo.getRemoteUserRecord(deviceId)
            ) {
                is Response.Success -> {
                    remoteUserRecord = response.data

                    val localRecord = repo.getLocalRecord()
                    syncUserRecords(localRecord, remoteUserRecord!!)
                }
                is Response.Error -> {
                    if (response.message == "Device not registered")
                        remoteUserRecord = repo.registerDevice(deviceId)
                }
            }

            if (remoteUserRecord == null) {
                _uiState.update { it.copy(
                    errorMessage = repo.errorFetchingDataStr,
                    loading = false
                )}
            }
            else {
                // fill items
                repo.getRanks().map {
                    items.add(
                        LeaderboardItem(
                            userId = "${repo.userStr} ${it.userId}",
                            readingRecord = "${it.readingRecord} ${repo.pagesStr}",
                            listeningRecord = formatTelawatTime(it.listeningRecord)
                        )
                    )
                }

                _uiState.update { it.copy(
                    userId = "${repo.userStr} ${remoteUserRecord!!.userId}",
                    loading = false
                )}
            }
        }
    }

    fun getSortedItems(rankType: RankType): List<LeaderboardItem> {
        when (rankType) {
            RankType.BY_READING -> items.sortByDescending { it.readingRecord }
            RankType.BY_LISTENING -> items.sortByDescending { it.listeningRecord }
        }

        _uiState.update { it.copy(
            userPosition = getUserPosition()
        )}

        return items
    }

    private fun getUserPosition(): String {
        return (items.indexOfFirst {
            it.userId == "${repo.userStr} ${remoteUserRecord!!.userId}"
        } + 1).toString()
    }

    private fun syncUserRecords(localRecord: UserRecord, remoteRecord: UserRecord) {
        val latestRecord = UserRecord(
            userId = remoteRecord.userId,
            readingRecord = max(localRecord.readingRecord, remoteRecord.readingRecord),
            listeningRecord = max(localRecord.listeningRecord, remoteRecord.listeningRecord)
        )

        if (latestRecord.readingRecord > remoteRecord.readingRecord ||
            latestRecord.listeningRecord > remoteRecord.listeningRecord) {
            viewModelScope.launch {
                repo.setRemoteUserRecord(deviceId, latestRecord)
            }
        }

        if (latestRecord.readingRecord > localRecord.readingRecord ||
            latestRecord.listeningRecord > localRecord.listeningRecord) {
            repo.setLocalQuranRecord(latestRecord.readingRecord)
            repo.setLocalTelawatRecord(latestRecord.listeningRecord)
        }
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    private fun formatTelawatTime(millis: Long): String {
        val hours = millis / (60 * 60 * 1000) % 24
        val minutes = millis / (60 * 1000) % 60
        val seconds = millis / 1000 % 60

        return LangUtils.translateNums(
            repo.numeralsLanguage,
            String.format(
                Locale.US, "%02d:%02d:%02d",
                hours, minutes, seconds
            )
        )
    }

}