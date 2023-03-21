package bassamalim.hidaya.core.models

import java.io.Serializable

data class Ayah(
    var id: Int,
    var juz: Int,
    val surahNum: Int,
    val ayahNum: Int,
    val surahName: String,
    var text: String?,
    var translation: String?,
    val tafseer: String
) : Serializable {
    var start = 0
    var end = 0
}