package bassamalim.hidaya.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MyFloatingActionButton(
    iconId: Int,
    description: String,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = description,
            modifier = Modifier
                .size(60.dp)
                .padding(12.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MyHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp
) {
    HorizontalDivider(
        modifier = modifier
            .alpha(0.6F)
            .padding(vertical = 5.dp),
        thickness = thickness,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun MyCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    isEnabled: Boolean = true
) {
    Checkbox(
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        enabled = isEnabled,
        colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
            uncheckedColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun MultiDrawableImage(
    drawables: List<Pair<Int, Color>>,
    modifier: Modifier = Modifier,
    contentDescription: String? = ""
) {
    Box(modifier) {
        drawables.forEach { (resId, color) ->
            Image(
                painter = painterResource(resId),
                contentDescription = contentDescription,
                colorFilter = ColorFilter.tint(color)
            )
        }
    }
}