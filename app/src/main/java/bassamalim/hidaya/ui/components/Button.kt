package bassamalim.hidaya.ui.components

import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
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
    colors: ButtonColors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.surface),
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textColor: Color = AppTheme.colors.text,
    elevation: Int = 10,
    enabled: Boolean = true,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    image: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(10.dp),
        elevation =  ButtonDefaults.elevation(
            defaultElevation = elevation.dp,
            pressedElevation = (elevation + 5).dp,
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
                modifier = Modifier.padding(innerPadding),
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
    modifier: Modifier = Modifier,
    description: String = "",
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
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
fun MyBackBtn() {
    val context = LocalContext.current

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
                onClick = {
                    (context as ComponentActivity).onBackPressedDispatcher.onBackPressed()
                },
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