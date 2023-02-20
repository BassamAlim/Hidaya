package bassamalim.hidaya.state

import bassamalim.hidaya.enums.DownloadState
import bassamalim.hidaya.models.Reciter

data class TelawatState(
    val items: List<Reciter> = emptyList(),
    val continueListeningText: String = "",
    val favs: List<Int> = emptyList(),
    val downloadStates: List<List<DownloadState>> = emptyList(),
    val selectedVersions: List<Boolean> = emptyList(),
    val isFiltered: Boolean = false,
    val filterDialogShown: Boolean = false
)
