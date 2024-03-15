package bassamalim.hidaya.core.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf

data class ReciterSura(
    val num: Int,
    val suraName: String,
    val searchName: String,
    val fav: MutableState<Int> = mutableIntStateOf(0),
)