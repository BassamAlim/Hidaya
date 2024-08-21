package bassamalim.hidaya.features.recitationRecitersMenu.ui

import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.features.quran.ui.LastPlayedMedia

data class RecitationRecitersMenuUiState(
    val lastPlayedMedia: LastPlayedMedia? = null,
    val downloadStates: Map<Int, DownloadState> = emptyMap(),
    val narrationSelections: Map<Int, Boolean> = emptyMap(),
    val searchText: String = "",
    val isFiltered: Boolean = false,
    val filterDialogShown: Boolean = false
)
