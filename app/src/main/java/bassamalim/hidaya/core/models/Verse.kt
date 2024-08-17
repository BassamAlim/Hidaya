package bassamalim.hidaya.core.models

import java.io.Serializable

data class Verse(
    var id: Int,
    val num: Int,
    var juzNum: Int,
    val suraNum: Int,
    val suraName: String,
    var text: String?,
    var translation: String?,
    val interpretation: String
) : Serializable {
    var start = 0
    var end = 0
}