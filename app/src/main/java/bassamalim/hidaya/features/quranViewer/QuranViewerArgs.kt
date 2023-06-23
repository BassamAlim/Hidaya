package bassamalim.hidaya.features.quranViewer

data class QuranViewerArgs(
    val type: String,
    val suraId: Int = -1,
    val pageNum: Int = -1
)
