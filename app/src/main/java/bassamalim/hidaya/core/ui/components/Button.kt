package bassamalim.hidaya.core.ui.components

import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.ui.theme.nsp

@Composable
fun PrimaryPillBtn(
    text: String,
    modifier: Modifier = Modifier,
    widthPercent: Float = 0.75f,
    tint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    fontSize: TextUnit = 22.sp,
    enabled: Boolean = true,
    padding: PaddingValues = PaddingValues(vertical = 16.dp),
    innerPadding: PaddingValues = PaddingValues(vertical = 3.dp),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(widthPercent)
            .padding(padding),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = tint
        ),
        elevation =  ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp,
            pressedElevation = 15.dp,
            disabledElevation = 0.dp
        ),
        enabled = enabled
    ) {
        MyText(
            text = text,
            modifier = Modifier.padding(innerPadding),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textColor = textColor
        )
    }
}

@Composable
fun SecondaryPillBtn(
    text: String,
    modifier: Modifier = Modifier,
    widthPercent: Float = 0.65f,
    tint: Color = MaterialTheme.colorScheme.secondary,
    textColor: Color = MaterialTheme.colorScheme.onSecondary,
    fontSize: TextUnit = 20.sp,
    enabled: Boolean = true,
    padding: PaddingValues = PaddingValues(vertical = 10.dp),
    innerPadding: PaddingValues = PaddingValues(vertical = 4.dp),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(widthPercent)
            .padding(padding),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = tint
        ),
        elevation =  ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp,
            pressedElevation = 15.dp,
            disabledElevation = 0.dp
        ),
        enabled = enabled
    ) {
        MyText(
            text = text,
            modifier = Modifier.padding(innerPadding),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textColor = textColor
        )
    }
}

@Composable
fun MyRectangleButton(
    text: String,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(3.dp),
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Int = 10,
    enabled: Boolean = true,
    innerPadding: PaddingValues = PaddingValues(6.dp),
    image: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(padding),
        colors = colors,
        shape = RoundedCornerShape(10.dp),
        elevation =  ButtonDefaults.buttonElevation(
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
fun MyHorizontalButton(
    text: String,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Int = 10,
    enabled: Boolean = true,
    middlePadding: PaddingValues = PaddingValues(6.dp),
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = {
            if (enabled) onClick()
        },
        modifier = modifier.padding(3.dp),
        colors = colors,
        shape = RoundedCornerShape(10.dp),
        elevation =  ButtonDefaults.buttonElevation(
            defaultElevation = elevation.dp,
            pressedElevation = (elevation + 5).dp,
            disabledElevation = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (icon != null) {
                icon()

                Spacer(modifier = Modifier.padding(middlePadding))
            }

            MyText(
                text = text,
                fontSize = fontSize,
                fontWeight = fontWeight,
                textColor = if (enabled) textColor else Color.Gray
            )
        }
    }
}

@Composable
fun MyIconButton(
    iconId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    innerPadding: Dp = 6.dp,
    description: String = "",
    tint: Color = LocalContentColor.current,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .clickable { if (isEnabled) onClick() }
    ) {
        Box(
            Modifier.padding(innerPadding)
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = description,
                modifier = Modifier.size(size),
                tint = tint,
            )
        }
    }
}

@Composable
fun MyIconButton(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    padding: Dp = 6.dp,
    description: String = "",
    tint: Color = LocalContentColor.current,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .clickable { if (isEnabled) onClick() }
    ) {
        Box(
            Modifier.padding(padding)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = description,
                modifier = Modifier.size(size),
                tint = tint,
            )
        }
    }
}

@Composable
fun MyBackButton(onClick: (() -> Unit)? = null) {
    val context = LocalContext.current

    Row(
        Modifier
            .fillMaxHeight()
            .width(72.dp - 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClick ?: {
                (context as ComponentActivity).onBackPressedDispatcher.onBackPressed()
            },
            enabled = true
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MySquareButton(
    text: String,
    drawableId: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 70.dp,
    onClick: () -> Unit
) {
    MyRectangleButton(
        text = text,
        fontSize = 18.nsp,
        modifier = Modifier
            .size(180.dp)
            .padding(vertical = 7.dp, horizontal = 7.dp),
        image = {
            Icon(
                painter = painterResource(drawableId),
                contentDescription = text,
                modifier = modifier.size(iconSize),
                tint = tint
            )
        },
        onClick = onClick
    )
}

@Composable
fun MyFavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    MyIconButton(
        iconId =
            if (isFavorite) R.drawable.ic_star
            else R.drawable.ic_star_outline,
        description = "Favorite",
        onClick = onClick,
        tint = MaterialTheme.colorScheme.primary,
        size = 28.dp
    )
}

@Composable
fun MyDownloadButton(
    state: DownloadState,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    onClick: () -> Unit
) {
    Box(
        modifier,
        contentAlignment = Alignment.Center
    ) {
        if (state == DownloadState.DOWNLOADING)
            MyCircularProgressIndicator(Modifier.size(size))
        else {
            MyIconButton(
                iconId =
                    if (state == DownloadState.DOWNLOADED) R.drawable.ic_downloaded
                    else R.drawable.ic_download,
                description = stringResource(R.string.download_description),
                size = size,
                innerPadding = 8.dp,
                tint =
                    if (state == DownloadState.DOWNLOADED) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onPrimary,
                onClick = onClick
            )
        }
    }
}

@Composable
fun MyImageButton(
    imageResId: Int,
    description: String = "",
    isEnabled: Boolean = true,
    padding: Dp = 14.dp,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { if (isEnabled) onClick() }
    ) {
        Image(
            painter = painterResource(imageResId),
            contentDescription = description,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun MyIconPlayerBtn(
    state: Int,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    padding: Dp = 0.dp,
    enabled: Boolean = true,
    playIcon: Int = R.drawable.ic_play,
    pauseIcon: Int = R.drawable.ic_pause,
    tint: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable { if (enabled) onClick() }
    ) {
        AnimatedContent(
            targetState = state,
            label = "",
            transitionSpec = {
                scaleIn(animationSpec = tween(durationMillis = 200)) togetherWith
                        scaleOut(animationSpec = tween(durationMillis = 200))
            }
        ) { state ->
            if (state == PlaybackStateCompat.STATE_NONE ||
                state == PlaybackStateCompat.STATE_CONNECTING ||
                state == PlaybackStateCompat.STATE_BUFFERING)
                MyCircularProgressIndicator()
            else {
                Icon(
                    painter = painterResource(
                        if (state == PlaybackStateCompat.STATE_PLAYING) pauseIcon
                        else playIcon
                    ),
                    contentDescription = stringResource(R.string.play_pause_btn_description),
                    modifier = Modifier.padding(padding),
                    tint = tint
                )
            }
        }
    }
}

@Composable
fun MyCloseBtn(
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    MyIconButton(
        iconId = R.drawable.ic_close,
        modifier = modifier,
        description = stringResource(R.string.close),
        innerPadding = 10.dp,
        tint = MaterialTheme.colorScheme.onPrimary,
        onClick = onClose
    )
}