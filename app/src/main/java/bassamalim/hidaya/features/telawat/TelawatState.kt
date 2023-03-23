package bassamalim.hidaya.features.telawat

import bassamalim.hidaya.core.enums.DownloadState

data class TelawatState(
    val continueListeningText: String = "",
    val favs: List<Int> = emptyList(),
    val downloadStates: List<List<DownloadState>> = emptyList(),
    val selectedVersions: List<Boolean> = emptyList(),
    val searchText: String = "",
    val isFiltered: Boolean = false,
    val filterDialogShown: Boolean = false
)
