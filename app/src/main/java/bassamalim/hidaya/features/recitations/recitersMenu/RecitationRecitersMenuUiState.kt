package bassamalim.hidaya.features.recitations.recitersMenu

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.PlaybackStatus
import bassamalim.hidaya.features.quran.surasMenu.RecitationInfo

data class RecitationRecitersMenuUiState(
    val isLoading: Boolean = true,
    /** Set when the menu is narrowed to one sura's recitations. */
    val suraName: String? = null,
    val numeralsLanguage: Language = Language.ARABIC,
    val playbackRecitationInfo: RecitationInfo? = null,
    val playbackState: PlaybackStatus = PlaybackStatus.CONNECTING,
    val searchText: String = "",
    val isFiltered: Boolean = false,
    val expandedReciterIds: Set<Int> = emptySet()
)