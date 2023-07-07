package bassamalim.hidaya.core.models

import java.io.Serializable

data class Aya(
    var id: Int,
    var juz: Int,
    val suraNum: Int,
    val suraName: String,
    val ayaNum: Int,
    var text: String?,
    var translation: String?,
    val tafseer: String
) : Serializable {
    var start = 0
    var end = 0
}