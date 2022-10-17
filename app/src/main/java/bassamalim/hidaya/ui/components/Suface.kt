package bassamalim.hidaya.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.ui.theme.AppTheme

@Composable
fun MySurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(cornerRadius),
        color = AppTheme.colors.surface,
        elevation = 10.dp,
    ) {
        content()
    }
}

@Composable
fun MyClickableSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(cornerRadius),
        color = AppTheme.colors.surface,
        elevation = 10.dp,
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MyBtnSurface(
    text: String,
    modifier: Modifier = Modifier,
    innerVPadding: Dp = 10.dp,
    fontSize: TextUnit = 20.sp,
    iconBtn: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(10.dp),
        color = AppTheme.colors.surface,
        elevation = 10.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = innerVPadding, horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyText(
                text = text,
                fontSize = fontSize,
                modifier = Modifier
                    .weight(1F)
                    .padding(10.dp)
            )

            iconBtn()
        }
    }
}