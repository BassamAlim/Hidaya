package bassamalim.hidaya.core.ui.components

import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.core.ui.theme.tajwal

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
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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
    iconSize: Dp = 24.dp,
    description: String = "",
    containerColor: Color = IconButtonDefaults.iconButtonColors().containerColor,
    contentColor: Color = IconButtonDefaults.iconButtonColors().contentColor,
    enabled: Boolean = true,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    onClick: () -> Unit
) {
    IconButton(
        onClick = { if (enabled) onClick() },
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = description,
            modifier = Modifier
                .size(iconSize)
                .padding(innerPadding)
        )
    }
}

@Composable
fun MyFilledIconButton(
    iconId: Int,
    modifier: Modifier = Modifier,
    description: String = "",
    containerColor: Color = IconButtonDefaults.filledIconButtonColors().containerColor,
    contentColor: Color = IconButtonDefaults.filledIconButtonColors().contentColor,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = { if (isEnabled) onClick() },
        modifier = modifier,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = description
        )
    }
}

@Composable
fun MyFilledTonalIconButton(
    iconId: Int,
    modifier: Modifier = Modifier,
    description: String = "",
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = { if (isEnabled) onClick() },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = description
        )
    }
}

@Composable
fun MyIconButton(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    description: String = "",
    contentColor: Color = LocalContentColor.current,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        onClick = { if (isEnabled) onClick() },
        modifier = modifier
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = description,
            modifier = iconModifier,
            tint = contentColor
        )
    }
}

@Composable
fun MyBackButton(onClick: (() -> Unit)? = null) {
    val context = LocalContext.current

    MyIconButton(Icons.AutoMirrored.Default.ArrowBackIos) {
        if (onClick == null)
                (context as ComponentActivity).onBackPressedDispatcher.onBackPressed()
        else
            onClick()
    }
}

@Composable
fun MySquareButton(
    text: String,
    imageVector: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = Dp.Unspecified,
    onClick: () -> Unit
) {
    MyRectangleButton(
        text = text,
        fontSize = 18.nsp,
        modifier = modifier
            .size(180.dp)
            .padding(vertical = 7.dp, horizontal = 7.dp),
        image = {
            Icon(
                imageVector = imageVector,
                contentDescription = text,
                modifier =
                    if (iconSize == Dp.Unspecified) Modifier
                    else Modifier.size(iconSize),
                tint = tint
            )
        },
        onClick = onClick
    )
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
        modifier = modifier
            .size(180.dp)
            .padding(vertical = 7.dp, horizontal = 7.dp),
        image = {
            Icon(
                painter = painterResource(drawableId),
                contentDescription = text,
                modifier =
                    if (iconSize == Dp.Unspecified) Modifier
                    else Modifier.size(iconSize),
                tint = tint
            )
        },
        onClick = onClick
    )
}

@Composable
fun MyFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector =
                if (isFavorite) Icons.Filled.Star
                else Icons.Outlined.StarOutline,
            contentDescription = stringResource(R.string.favorite),
            modifier = Modifier.size(size),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MyDownloadButton(
    state: DownloadState,
    modifier: Modifier = Modifier,
    iconSize: Dp = 26.dp,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (state == DownloadState.DOWNLOADING)
            MyCircularProgressIndicator(Modifier.size(iconSize))
        else {
            MyIconButton(
                imageVector =
                    if (state == DownloadState.DOWNLOADED) Icons.Default.DownloadDone
                    else Icons.Default.Download,
                description = stringResource(R.string.download_description),
                iconModifier = Modifier.size(iconSize),
                onClick = onClick,
                contentColor = contentColor
            )
        }
    }
}

@Composable
fun MyIconPlayerButton(
    state: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = Dp.Unspecified,
    enabled: Boolean = true,
    filled: Boolean = true,
    tint: Color = LocalContentColor.current
) {
    IconButton(
        onClick = { if (enabled) onClick() },
        modifier = modifier
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
                    imageVector =
                        if (filled) {
                            if (state == PlaybackStateCompat.STATE_PLAYING) Icons.Default.PauseCircleFilled
                            else Icons.Default.PlayCircleFilled
                        }
                        else {
                            if (state == PlaybackStateCompat.STATE_PLAYING) Icons.Default.Pause
                            else Icons.Default.PlayArrow
                        },
                    contentDescription = stringResource(R.string.play_pause_btn_description),
                    modifier = Modifier.size(iconSize),
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
        imageVector = Icons.Default.Close,
        modifier = modifier,
        description = stringResource(R.string.close),
        contentColor = MaterialTheme.colorScheme.onPrimary,
        onClick = onClose
    )
}

@Composable
fun MyTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    textColor: Color = MaterialTheme.colorScheme.primary,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Center,
    fontFamily: FontFamily = tajwal,
    textModifier: Modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = textModifier,
            fontSize = fontSize,
            style = TextStyle(
                fontFamily = fontFamily,
                color = textColor,
                fontWeight = fontWeight,
                textAlign = textAlign,
                lineHeight = fontSize * 1.4
            )
        )
    }
}