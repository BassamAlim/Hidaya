package bassamalim.hidaya.state

import bassamalim.hidaya.enums.DownloadState
import bassamalim.hidaya.enums.ListType
import bassamalim.hidaya.models.Reciter

data class TelawatState(
    val items: List<Reciter> = emptyList(),
    val listType: ListType = ListType.All,
    val continueListeningText: String = "",
    val favs: List<Int> = emptyList(),
    val downloadStates: List<MutableList<DownloadState>> = emptyList(),
    val selectedVersions: List<Boolean> = emptyList(),
    val isFiltered: Boolean = false,
    val filterDialogShown: Boolean = false
)
