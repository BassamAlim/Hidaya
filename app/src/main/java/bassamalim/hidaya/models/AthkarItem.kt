package bassamalim.hidaya.models

import androidx.compose.runtime.MutableState

data class AthkarItem(
    val id: Int,
    val category_id: Int,
    val name: String,
    var favorite: MutableState<Int>
)