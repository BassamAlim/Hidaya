package bassamalim.hidaya.features.recitationsRecitersMenu

import bassamalim.hidaya.core.enums.DownloadState

data class RecitationsRecitersMenuState(
    val continueListeningText: String = "",
    val downloadStates: Map<Int, DownloadState> = emptyMap(),
    val selectedVersions: List<Boolean> = emptyList(),
    val searchText: String = "",
    val isFiltered: Boolean = false,
    val filterDialogShown: Boolean = false
)
