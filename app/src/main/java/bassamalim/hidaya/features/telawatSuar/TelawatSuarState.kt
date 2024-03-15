package bassamalim.hidaya.features.telawatSuar

import bassamalim.hidaya.core.enums.DownloadState

data class TelawatSuarState(
    val title: String = "",
    val downloadStates: List<DownloadState> = emptyList(),
    val searchText: String = ""
)