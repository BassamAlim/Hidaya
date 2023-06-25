package bassamalim.hidaya.features.quranViewer

data class QuranViewerNavArgs(
    val type: String,
    val suraId: Int = -1,
    val pageNum: Int = -1
)
