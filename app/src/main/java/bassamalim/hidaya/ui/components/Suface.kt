package bassamalim.hidaya.ui.components

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
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.ui.theme.AppTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MySurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick == null) {
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
    else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 8.dp),
            shape = RoundedCornerShape(10.dp),
            color = AppTheme.colors.surface,
            elevation = 10.dp,
            onClick = onClick
        ) {
            content()
        }
    }
}

@Composable
fun MyBtnSurface(
    text: String,
    modifier: Modifier = Modifier,
    innerVPadding: Dp = 10.dp,
    iconBtn: @Composable () -> Unit,
    onClick: () -> Unit
) {
    MySurface(
        modifier = modifier,
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
                modifier = Modifier
                    .weight(1F)
                    .padding(10.dp)
            )

            iconBtn()
        }
    }
}