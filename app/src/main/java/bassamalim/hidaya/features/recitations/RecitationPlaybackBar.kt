package bassamalim.hidaya.features.recitations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.PlaybackStatus
import bassamalim.hidaya.core.ui.components.MyCloseButton
import bassamalim.hidaya.core.ui.components.MyIconPlayerButton
import bassamalim.hidaya.core.ui.theme.appTypography
import bassamalim.hidaya.core.ui.theme.dimensions
import bassamalim.hidaya.features.quran.surasMenu.RecitationInfo

val RecitationPlaybackBarHeight = 68.dp

/** The now-playing bar shared by the reciters menu and the Quran suras menu. */
@Composable
fun RecitationPlaybackBar(
    recitationInfo: RecitationInfo?,
    playbackState: PlaybackStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    /** Shows a close button in place of the arrow. */
    onDismiss: (() -> Unit)? = null
) {
    val dims = MaterialTheme.dimensions

    AnimatedVisibility(
        visible = recitationInfo != null,
        modifier = modifier,
        enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
        exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
    ) {
        if (recitationInfo == null) return@AnimatedVisibility

        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = dims.spaceMd, end = dims.spaceMd, bottom = dims.spaceMd)
                .height(RecitationPlaybackBarHeight),
            shape = RoundedCornerShape(dims.radiusLg),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shadowElevation = dims.elevationLg
        ) {
            Row(
                modifier = Modifier.padding(horizontal = dims.spaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    MyIconPlayerButton(
                        state = playbackState,
                        onClick = onPlayPauseClick,
                        iconSize = dims.iconLg,
                        filled = false,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = dims.spaceMd)
                ) {
                    Text(
                        text = "${stringResource(R.string.sura)} ${recitationInfo.suraName}",
                        style = MaterialTheme.appTypography.subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${stringResource(R.string.for_reciter)} " +
                                recitationInfo.reciterName,
                        style = MaterialTheme.appTypography.caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (onDismiss != null) MyCloseButton(onClose = onDismiss)
                else Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.continue_listening),
                    modifier = Modifier.size(dims.iconMd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
