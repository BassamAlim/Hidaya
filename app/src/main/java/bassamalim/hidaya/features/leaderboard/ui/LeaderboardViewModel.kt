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

    private lateinit var numeralsLanguage: Language

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

            val userRecord = domain.getUserRecord().data
            val ranks = domain.getRanks()
            println("ranks: ${ranks[RankType.BY_READING]!!.data}")

            val userRank =
                if (userRecord == null) emptyMap()
                else {
                    domain.getUserRank(userRecord, ranks).map { (rankType, rank) ->
                        rankType to rank.toString()
                    }.toMap()
                }

            val isError = userRecord == null || userRecord.userId == -1
                    || ranks.values.any { it is Response.Error<*> }
            println("isError: $isError")

            val ranksList = if (isError) mapOf(
                RankType.BY_READING to emptyList(),
                RankType.BY_LISTENING to emptyList()
            )
            else mapOf(
                RankType.BY_READING to ranks[RankType.BY_READING]!!.data!!
                    .map { (userId, value) ->
                        userId.toString() to translateNums(value.toString(), numeralsLanguage)
                    }.toList(),
                RankType.BY_LISTENING to ranks[RankType.BY_LISTENING]!!.data!!
                    .map { (userId, value) ->
                        userId.toString() to formatRecitationsTime(value)
                    }.toList()
            )
            println("ranksList: $ranksList")

            _uiState.update { it.copy(
                isLoading = false,
                isError = isError,
                userId = if (isError) "-1" else userRecord?.userId.toString(),
                userRanks = userRank,
                ranks = ranksList
            )}
        }
    }

    fun loadMore(rankType: RankType) {
        println("loadMore: $rankType")

        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoadingItems = it.isLoadingItems.toMutableMap().also { map ->
                    map[rankType] = true
                }
            )}

            val newRanksMap = domain.getMoreRanks(rankType = rankType).data ?: emptyMap()
            val newRanks = newRanksMap.map { (userId, value) ->
                userId.toString() to translateNums(value.toString(), numeralsLanguage)
            }.toList()
            println("newRanks: $newRanks")

            val added = _uiState.value.ranks[rankType]!! + newRanks

            println("added: $added")
            println("added size: ${added.size}")

            _uiState.update { it.copy(
                isLoadingItems = it.isLoadingItems.toMutableMap().also { map ->
                    map[rankType] = false
                },
                ranks = it.ranks.toMutableMap().also { map ->
                    map[rankType] = added
                }
            )}

            println("ranks size: ${_uiState.value.ranks[RankType.BY_READING]!!.size}")
            println("ranks: ${_uiState.value.ranks[RankType.BY_READING]!!}")
        }
    }

    private fun formatRecitationsTime(millis: Long): String {
        val hours = millis / (60 * 60 * 1000) % 24
        val minutes = millis / (60 * 1000) % 60
        val seconds = millis / 1000 % 60

        return translateNums(
            numeralsLanguage = numeralsLanguage,
            string = String.format(
                Locale.US, "%02d:%02d:%02d",
                hours, minutes, seconds
            )
        )
    }

}