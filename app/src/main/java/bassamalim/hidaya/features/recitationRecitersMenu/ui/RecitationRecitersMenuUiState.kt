package bassamalim.hidaya.features.recitationRecitersMenu.ui

import bassamalim.hidaya.core.enums.DownloadState

data class RecitationRecitersMenuUiState(
    val continueListeningText: String = "",
    val downloadStates: Map<Int, DownloadState> = emptyMap(),
    val narrationSelections: Map<Int, Boolean> = emptyMap(),
    val searchText: String = "",
    val isFiltered: Boolean = false,
    val filterDialogShown: Boolean = false
)
