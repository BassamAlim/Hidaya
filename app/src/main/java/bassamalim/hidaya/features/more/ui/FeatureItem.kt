package bassamalim.hidaya.features.more.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class FeatureItem(
    val title: String,
    val icon: ImageVector? = null,
    val drawableId: Int? = null,
    val onClick: () -> Unit,
    val color: Color? = null
)