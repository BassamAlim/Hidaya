package bassamalim.hidaya.features.recitations.surasMenu.ui

import bassamalim.hidaya.core.enums.DownloadState

data class RecitationsSurasUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val downloadStates: Map<Int, DownloadState> = emptyMap(),
    val searchText: String = ""
)