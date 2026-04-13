package bassamalim.hidaya.features.recitations.surasMenu

import bassamalim.hidaya.core.enums.DownloadState

data class RecitationSurasUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val downloadStates: Map<Int, DownloadState> = emptyMap(),
    val searchText: String = ""
)