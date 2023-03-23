package bassamalim.hidaya.features.telawatSuar

import bassamalim.hidaya.core.enums.DownloadState

data class TelawatSuarState(
    val title: String = "",
    val favs: List<Int> = emptyList(),
    val downloadStates: List<DownloadState> = emptyList(),
    val searchText: String = ""
)