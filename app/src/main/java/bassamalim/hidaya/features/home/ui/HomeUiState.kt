package bassamalim.hidaya.features.home.ui

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.TimeOfDay

data class HomeUiState(
    val isLoading: Boolean = true,
    val pendingPermissions: List<PendingPermission> = emptyList(),
    val previousPrayerName: String = "",
    val previousPrayerTimeText: String = "",
    val passed: String = "",
    val nextPrayerName: String = "",
    val nextPrayerTimeText: String = "",
    val remaining: String = "",
    val previousPrayerTime: TimeOfDay? = null,
    val nextPrayerTime: TimeOfDay? = null,
    val werdPage: String = "",
    val isWerdDone: Boolean = false,
    val quranRecord: String = "",
    val recitationsRecord: String = "",
    val isLeaderboardEnabled: Boolean = false,
    val language: Language = Language.ARABIC,
    val numeralsLanguage: Language = Language.ARABIC
)