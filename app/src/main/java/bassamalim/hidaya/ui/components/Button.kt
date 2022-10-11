package bassamalim.hidaya.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.theme.AppTheme

@Composable
fun MyButton(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textColor: Color = AppTheme.colors.text,
    enabled: Boolean = true,
    image: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.surface),
        shape = RoundedCornerShape(10.dp),
        elevation =  ButtonDefaults.elevation(
            defaultElevation = 10.dp,
            pressedElevation = 15.dp,
            disabledElevation = 0.dp
        ),
        enabled = enabled
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            image()

            MyText(
                text = text,
                modifier = Modifier.padding(6.dp),
                fontSize = fontSize,
                fontWeight = fontWeight,
                textColor = textColor
            )
        }

    }
}

@Composable
fun MyIconBtn(
    iconId: Int,
    description: String = "",
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = iconId),
            tint = tint,
            contentDescription = description
        )
    }
}

@Composable
fun MyIconBtn(
    imageVector: ImageVector,
    description: String = "",
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = imageVector,
            tint = tint,
            contentDescription = description
        )
    }
}

@Composable
fun MyBackBtn(
    onBackPressed: () -> Unit
) {
    Row(
        Modifier
            .fillMaxHeight()
            .width(72.dp - 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(
            LocalContentAlpha provides ContentAlpha.high,
        ) {
            IconButton(
                onClick = onBackPressed,
                enabled = true
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = AppTheme.colors.onPrimary
                )
            }
        }
    }
}

@Composable
fun MySquareButton(
    textResId: Int,
    imageResId: Int,
    onClick: () -> Unit
) {
    MyButton(
        text = stringResource(id = textResId),
        modifier = Modifier
            .size(180.dp)
            .padding(vertical = 10.dp, horizontal = 10.dp),
        image = {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = stringResource(id = textResId),
                modifier = Modifier.size(70.dp)
            )
        },
        onClick = onClick
    )
}

@Composable
fun MyFavBtn(
    fav: Int,
    onClick: () -> Unit
) {
    val iconId =
        if (fav == 1) R.drawable.ic_star
        else R.drawable.ic_star_outline

    MyIconBtn(
        iconId = iconId,
        description = "Favorite",
        onClick = onClick,
        tint = AppTheme.colors.accent
    )
}