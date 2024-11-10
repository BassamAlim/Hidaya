package bassamalim.hidaya.core.models

import java.io.Serializable

data class Verse(
    var id: Int,
    val num: Int,
    var juzNum: Int,
    val suraNum: Int,
    val suraName: String,
    var text: String?,
    val startLineNum: Int,
    val endLineNum: Int,
    var translation: String?,
    val interpretation: String
) : Serializable