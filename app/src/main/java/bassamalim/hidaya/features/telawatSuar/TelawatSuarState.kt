package bassamalim.hidaya.features.telawatSuar

import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.models.ReciterSura

data class TelawatSuarState(
    val title: String = "",
    val items: List<ReciterSura> = emptyList(),
    val favs: List<Int> = emptyList(),
    val downloadStates: List<DownloadState> = emptyList()
)