package bassamalim.hidaya.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
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
import bassamalim.hidaya.ui.theme.AppTheme

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
        color = AppTheme.colors.surface,
        elevation = 8.dp
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
        modifier = modifier
            .fillMaxWidth()
            .padding(padding),
        shape = RoundedCornerShape(cornerRadius),
        color = AppTheme.colors.surface,
        elevation = elevation,
    ) {
        Box(
            Modifier.clickable { onClick() }
        ) {
            content()
        }
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
            .padding(vertical = 5.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(10.dp),
        color = AppTheme.colors.surface,
        elevation = 6.dp,
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

@Composable
fun ExpandableCard(
    titleResId: Int,
    modifier: Modifier = Modifier,
    expandedContent: @Composable () -> Unit
) {
    var expandedState by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(if (expandedState) 180f else 0f)

    MyClickableSurface(
        modifier.animateContentSize(
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
                    stringResource(titleResId),
                    Modifier.weight(6f),
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.select),
                    tint = AppTheme.colors.text,
                    modifier = Modifier
                        .weight(1f)
                        .rotate(rotationState)
                )
            }

            if (expandedState) expandedContent()
        }
    }

    /*Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = TweenSpec(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = shape,
        onClick = {
            expandedState = !expandedState
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(6f),
                    text = title,
                    fontSize = titleFontSize,
                    fontWeight = titleFontWeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    modifier = Modifier
                        .alpha(ContentAlpha.medium)
                        .weight(1f)
                        .rotate(rotationState),
                    onClick = {
                        expandedState = !expandedState
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Drop Down Arrow"
                    )
                }
            }

            if (expandedState) expandedContent()
        }
    }*/
}