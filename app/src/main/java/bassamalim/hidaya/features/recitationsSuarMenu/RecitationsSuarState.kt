package bassamalim.hidaya.features.recitationsSuarMenu

import bassamalim.hidaya.core.enums.DownloadState

data class RecitationsSuarState(
    val title: String = "",
    val downloadStates: List<DownloadState> = emptyList(),
    val searchText: String = ""
)