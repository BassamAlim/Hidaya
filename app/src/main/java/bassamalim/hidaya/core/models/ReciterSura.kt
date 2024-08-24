package bassamalim.hidaya.core.models

import bassamalim.hidaya.core.enums.DownloadState

data class ReciterSura(
    val id: Int,
    val suraName: String,
    val searchName: String,
    val isFavorite: Boolean = false,
    val downloadState: DownloadState = DownloadState.NOT_DOWNLOADED
)