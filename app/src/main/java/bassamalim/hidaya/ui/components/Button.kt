package bassamalim.hidaya.ui.components

import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
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
import bassamalim.hidaya.enum.DownloadState
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
import bassamalim.hidaya.utils.FileUtils

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
    innerPadding: PaddingValues = PaddingValues(6.dp),
    image: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(3.dp),
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
    size: Dp = 24.dp,
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
            contentDescription = description,
            modifier = Modifier.size(size)
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
fun MyBackBtn(onClick: (() -> Unit)? = null) {
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
                onClick = onClick ?: {
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
        text = stringResource(textResId),
        fontSize = 18.nsp,
        modifier = Modifier
            .size(180.dp)
            .padding(vertical = 7.dp, horizontal = 7.dp),
        image = {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = stringResource(textResId),
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
        tint = AppTheme.colors.accent,
        size = 28.dp
    )
}

@Composable
fun MyDownloadBtn(
    state: DownloadState,
    path: String,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    tint: Color = AppTheme.colors.accent,
    deleted: () -> Unit,
    download: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (state == DownloadState.Downloading) MyCircularProgressIndicator(Modifier.size(size))
        else {
            MyIconBtn(
                iconId =
                    if (state == DownloadState.Downloaded) R.drawable.ic_downloaded
                    else R.drawable.ic_download,
                description = stringResource(R.string.download_description),
                tint = tint,
                size = size
            ) {
                if (state == DownloadState.Downloaded) {
                    deleted()
                    FileUtils.deleteFile(context, path)
                }
                else download()
            }
        }
    }

}

@Composable
fun MyImageButton(
    imageResId: Int,
    description: String = "",
    enabled: Boolean = true,
    padding: Dp = 14.dp,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { if (enabled) onClick() }
    ) {
        Image(
            painter = painterResource(imageResId),
            contentDescription = description,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun MyPlayerBtn(
    state: MutableState<Int>,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    padding: Dp = 5.dp,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable { if (enabled) onClick() }
    ) {
        if (state.value == PlaybackStateCompat.STATE_NONE ||
            state.value == PlaybackStateCompat.STATE_BUFFERING)
            MyCircularProgressIndicator()
        else {
            Image(
                painter = painterResource(
                    if (state.value == PlaybackStateCompat.STATE_PLAYING) R.drawable.ic_player_pause
                    else R.drawable.ic_player_play
                ),
                contentDescription = stringResource(R.string.play_pause_btn_description),
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun MyCloseBtn(
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    MyIconBtn(
        iconId = R.drawable.ic_close,
        modifier = modifier,
        description = stringResource(R.string.close),
        onClick = onClose,
        tint = AppTheme.colors.text
    )
}