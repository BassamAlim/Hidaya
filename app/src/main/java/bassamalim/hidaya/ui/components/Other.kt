package bassamalim.hidaya.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.Grey

@Composable
fun MyCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.accent
) {
    CircularProgressIndicator(
        color = color,
        modifier = modifier
    )
}

@Composable
fun MyFloatingActionButton(
    iconId: Int,
    description: String,
    onClick: () -> Unit
) {
    FloatingActionButton(
        backgroundColor = AppTheme.colors.primary,
        contentColor = AppTheme.colors.onPrimary,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = description,
            modifier = Modifier
                .size(60.dp)
                .padding(12.dp),
            tint = AppTheme.colors.secondary
        )
    }
}

@Composable
fun MyHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp
) {
    Divider(
        thickness = thickness,
        color = Grey,
        modifier = modifier
            .alpha(0.6F)
            .padding(vertical = 5.dp)
    )
}

@Composable
fun MyCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Checkbox(
        checked = isChecked,
        onCheckedChange = { onCheckedChange(it) },
        colors = CheckboxDefaults.colors(
            checkedColor = AppTheme.colors.accent,
            uncheckedColor = AppTheme.colors.text
        )
    )
}