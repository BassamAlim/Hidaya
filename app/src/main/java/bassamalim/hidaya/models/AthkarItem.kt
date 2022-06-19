package bassamalim.hidaya.models

import android.view.View

data class AthkarItem(
    val id: Int, val category_id: Int, val name: String, var favorite: Int,
    val listener: View.OnClickListener
)