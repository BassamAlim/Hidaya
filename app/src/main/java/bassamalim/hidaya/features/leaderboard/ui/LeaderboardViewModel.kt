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
            val userRank =
                if (userRecord == null) emptyMap()
                else {
                    domain.getUserRank(userRecord, ranks).map { (rankType, rank) ->
                        rankType to rank.toString()
                    }.toMap()
                }

            val isError = userRecord == null || userRecord.userId == -1
                    || ranks.values.any { it is Response.Error<*> }

            _uiState.update { it.copy(
                isLoading = false,
                isError = isError,
                userId = if (isError) "-1" else userRecord?.userId.toString(),
                userRanks = userRank,
                ranks =
                    if (isError) emptyMap()
                    else ranks.map { (rankType, rank) ->
                        rankType to rank.data!!.map { record ->
                            LeaderboardItem(
                                userId = record.userId.toString(),
                                quranRecord = record.quranPages.toString(),
                                recitationsRecord =
                                    formatRecitationsTime(userRecord?.recitationsTime ?: 0)
                            )
                        }
                    }.toMap()
            )}
        }
    }

    fun getTabContent(rankType: RankType): LeaderboardTabContent {
        return LeaderboardTabContent(
            userRank = _uiState.value.userRanks[rankType] ?: "",
            items = _uiState.value.ranks[rankType] ?: emptyList()
        )
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