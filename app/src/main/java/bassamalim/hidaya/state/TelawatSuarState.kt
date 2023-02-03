package bassamalim.hidaya.state

import bassamalim.hidaya.enum.DownloadState
import bassamalim.hidaya.models.ReciterSura

data class TelawatSuarState(
    val title: String = "",
    val items: List<ReciterSura> = emptyList(),
    val favs: List<Int> = emptyList(),
    val downloadStates: List<DownloadState> = emptyList()
)