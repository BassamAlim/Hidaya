package bassamalim.hidaya.features.leaderboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LeaderboardVM @Inject constructor(
    app: Application,
    private val repo: LeaderboardRepo,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(app) {

    private val userRecord = UserRecord(
        userId = savedStateHandle.get<Int>("user_id") ?: -1,
        readingRecord = savedStateHandle.get<Int>("reading_record") ?: 0,
        listeningRecord = savedStateHandle.get<Long>("listening_record") ?: 0L
    )

    private lateinit var items: MutableList<LeaderboardItem>

    private val _uiState = MutableStateFlow(LeaderboardState())
    val uiState = _uiState.asStateFlow()

    init {
        fillRanks()
    }

    private fun fillRanks() {
        viewModelScope.launch {
            if (userRecord.userId == -1) {
                _uiState.update { it.copy(
                    errorMessage = repo.errorFetchingDataStr,
                    loading = false
                )}
            }
            else {
                items = mutableListOf()

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
                    userId = "${repo.userStr} ${userRecord.userId}",
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
            it.userId == "${repo.userStr} ${userRecord.userId}"
        } + 1).toString()
    }

    private fun formatTelawatTime(millis: Long): String {
        val hours = millis / (60 * 60 * 1000) % 24
        val minutes = millis / (60 * 1000) % 60
        val seconds = millis / 1000 % 60

        return translateNums(
            repo.numeralsLanguage,
            String.format(
                Locale.US, "%02d:%02d:%02d",
                hours, minutes, seconds
            )
        )
    }

}