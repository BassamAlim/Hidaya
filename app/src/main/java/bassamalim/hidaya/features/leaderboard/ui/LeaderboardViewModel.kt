package bassamalim.hidaya.features.leaderboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Response
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.leaderboard.domain.LeaderboardDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val domain: LeaderboardDomain
): ViewModel() {

    lateinit var numeralsLanguage: Language

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = LeaderboardUiState()
    )

    private fun initializeData() {
        viewModelScope.launch {
            numeralsLanguage = domain.getNumeralsLanguage()

            val userRecord = domain.getUserRecord()?.data
            val ranks = domain.getRanks()

            val isError = userRecord == null || userRecord.userId == -1
                    || ranks == null || ranks.values.any { it is Response.Error<*> }
            if (isError) {
                _uiState.update { it.copy(
                    isLoading = false,
                    isError = true
                )}
                return@launch
            }

            val userRank = domain.getUserRank(userRecord.userId)
                .mapValues { (_, rank) -> 
                    if (rank == -1) translateNums("--", numeralsLanguage)
                    else translateNums(rank.toString(), numeralsLanguage)
                }

            val ranksList = mapOf(
                RankType.BY_READING to (ranks[RankType.BY_READING]?.data ?: emptyMap())
                    .map { (userId, value) ->
                        translateNums(userId.toString(), numeralsLanguage) to
                                translateNums(value.toString(), numeralsLanguage)
                    },
                RankType.BY_LISTENING to (ranks[RankType.BY_LISTENING]?.data ?: emptyMap())
                    .map { (userId, value) ->
                        translateNums(userId.toString(), numeralsLanguage) to
                                formatRecitationsTime(value)
                    }
            )

            _uiState.update { it.copy(
                isLoading = false,
                userId = translateNums(userRecord.userId.toString(), numeralsLanguage),
                userRanks = userRank,
                ranks = ranksList
            )}
        }
    }

    fun loadMore(rankType: RankType) {
        if (_uiState.value.isLoadingItems[rankType] == true) return

        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoadingItems = it.isLoadingItems.toMutableMap().also { map ->
                    map[rankType] = true
                }
            )}

            val newRanksMap = domain.getMoreRanks(rankType = rankType)?.data ?: emptyMap()
            val newRanks = when (rankType) {
                RankType.BY_READING -> newRanksMap.map { (userId, value) ->
                    translateNums(userId.toString(), numeralsLanguage) to
                            translateNums(value.toString(), numeralsLanguage)
                }
                RankType.BY_LISTENING -> newRanksMap.map { (userId, value) ->
                    translateNums(userId.toString(), numeralsLanguage) to
                            formatRecitationsTime(value)
                }
            }

            _uiState.update { it.copy(
                isLoadingItems = it.isLoadingItems.toMutableMap().also { map ->
                    map[rankType] = false
                },
                ranks = it.ranks.toMutableMap().also { map ->
                    map[rankType] = (it.ranks[rankType] ?: emptyList()) + newRanks
                }
            )}
        }
    }

    private fun formatRecitationsTime(millis: Long): String {
        val hours = millis / (60 * 60 * 1000)
        val minutes = millis / (60 * 1000) % 60
        val seconds = millis / 1000 % 60

        return translateNums(
            numeralsLanguage = numeralsLanguage,
            string = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        )
    }

}