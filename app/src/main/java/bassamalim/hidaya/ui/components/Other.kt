package bassamalim.hidaya.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
    color: Color = AppTheme.colors.accent,
    padding: Dp = 0.dp
) {
    CircularProgressIndicator(
        color = color,
        modifier = Modifier
            .padding(padding)
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
            tint = AppTheme.colors.accent
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
    state: MutableState<Boolean>,
    onCheck: () -> Unit = {}
) {
    Checkbox(
        checked = state.value,
        onCheckedChange = {
            state.value = it
            onCheck()
        },
        colors = CheckboxDefaults.colors(
            checkedColor = AppTheme.colors.accent,
            uncheckedColor = AppTheme.colors.text
        )
    )
}