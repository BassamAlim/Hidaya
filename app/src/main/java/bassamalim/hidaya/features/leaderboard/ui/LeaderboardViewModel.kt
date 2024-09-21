package bassamalim.hidaya.features.leaderboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
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

            val userId = domain.fetchData()
            _uiState.update { it.copy(
                isLoading = false,
                isError = userId == -1,
                userId = userId.toString(),
            )}
        }
    }

    fun getTabContent(rankType: RankType): LeaderboardTabContent {
        _uiState.update { it.copy(
            isLoading = true
        )}

        val ranks = domain.getSortedRanks(sortBy = rankType)

        val items = ranks.map {
            LeaderboardItem(
                userId = it.userId.toString(),
                quranRecord = it.quranPages.toString(),
                recitationsRecord = formatRecitationsTime(it.recitationsTime)
            )
        }

        _uiState.update { it.copy(
            isLoading = false
        )}

        return LeaderboardTabContent(
            userRank = domain.getUserRank(ranks).toString(),
            items = items
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