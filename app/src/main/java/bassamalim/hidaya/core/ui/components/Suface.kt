package bassamalim.hidaya.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R

@Composable
fun MySurface(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(vertical = 6.dp, horizontal = 8.dp),
    cornerRadius: Dp = 10.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(padding),
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 8.dp
    ) {
        content()
    }
}

@Composable
fun MyClickableSurface(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(vertical = 3.dp, horizontal = 8.dp),
    cornerRadius: Dp = 10.dp,
    elevation: Dp = 6.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.padding(padding),
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = elevation,
    ) {
        Box(
            modifier = Modifier.clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun MyButtonSurface(
    text: String,
    modifier: Modifier = Modifier,
    innerVPadding: Dp = 10.dp,
    fontSize: TextUnit = 20.sp,
    iconButton: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 6.dp,
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

            iconButton()
        }
    }
}

@Composable
fun ExpandableCard(
    title: String,
    modifier: Modifier = Modifier,
    expandedContent: @Composable () -> Unit
) {
    var expandedState by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(if (expandedState) 180f else 0f, label = "")

    MyClickableSurface(
        modifier = modifier.animateContentSize(
            animationSpec = TweenSpec(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        ),
        onClick = { expandedState = !expandedState }
    ) {
        MyFatColumn {
            Row(
                Modifier.padding(vertical = 25.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyText(
                    title,
                    Modifier.weight(6f),
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.select),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .rotate(rotationState)
                )
            }

            if (expandedState) expandedContent()
        }
    }
}