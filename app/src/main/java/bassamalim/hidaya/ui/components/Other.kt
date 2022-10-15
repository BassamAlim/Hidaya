package bassamalim.hidaya.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
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
                .padding(12.dp)
        )
    }
}

@Composable
fun MyHorizontalDivider() {
    Divider(
        thickness = 1.dp,
        color = Grey,
        modifier = Modifier
            .alpha(0.5F)
            .padding(vertical = 5.dp)
    )
}