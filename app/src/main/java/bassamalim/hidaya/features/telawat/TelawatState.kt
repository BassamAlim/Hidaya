package bassamalim.hidaya.features.telawat

import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.models.Reciter

data class TelawatState(
    val items: List<Reciter> = emptyList(),
    val continueListeningText: String = "",
    val favs: List<Int> = emptyList(),
    val downloadStates: List<List<DownloadState>> = emptyList(),
    val selectedVersions: List<Boolean> = emptyList(),
    val isFiltered: Boolean = false,
    val filterDialogShown: Boolean = false
)
