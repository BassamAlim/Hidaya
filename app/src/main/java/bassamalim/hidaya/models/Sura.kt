package bassamalim.hidaya.models

import androidx.compose.runtime.MutableState

data class Sura(
    val id: Int,
    val suraName: String,
    val searchName: String,
    val tanzeel: Int,
    var favorite: MutableState<Int>
)