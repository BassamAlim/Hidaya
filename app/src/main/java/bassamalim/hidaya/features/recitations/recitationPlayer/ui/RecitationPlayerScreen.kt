package bassamalim.hidaya.features.recitations.recitationPlayer.ui

import android.app.Activity
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.ui.components.MyDownloadBtn
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyIconPlayerBtn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySlider
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecitationPlayerScreen(
    viewModel: RecitationPlayerViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose(viewModel::onStop)
    }

    MyScaffold(
        title = stringResource(R.string.recitations),
        bottomBar = {
            BottomBar(
                repeatMode = state.repeatMode,
                shuffleMode = state.shuffleMode,
                downloadState = state.downloadState,
                onRepeatClick = viewModel::onRepeatClick,
                onShuffleClick = viewModel::onShuffleClick,
                onDownloadClick = viewModel::onDownloadClick
            )
        },
        onBack = { viewModel.onBackPressed(activity) }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InfoSpace(
                suraName = state.suraName,
                narrationName = state.narrationName,
                reciterName = state.reciterName
            )

            ProgressSpace(
                progress = state.progress,
                duration = state.duration,
                playbackState = state.btnState,
                areControlsEnabled = state.controlsEnabled,
                onSliderChange = viewModel::onSliderChange,
                onSliderChangeFinished = viewModel::onSliderChangeFinished,
                onPreviousTrackClick = viewModel::onPreviousTrackClick,
                onPlayPauseClick = viewModel::onPlayPauseClick,
                onNextTrackClick = viewModel::onNextTrackClick
            )
        }
    }
}

@Composable
private fun InfoSpace(
    suraName: String,
    narrationName: String,
    reciterName: String
) {
    Box(
        Modifier.border(
            width = 2.dp,
            shape = RoundedCornerShape(10),
            color = AppTheme.colors.accent
        )
    ) {
        Column(
            Modifier.padding(vertical = 25.dp, horizontal = 75.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MyText(
                text = suraName,
                modifier = Modifier.padding(vertical = 10.dp),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            MyText(
                text = narrationName,
                modifier = Modifier.padding(vertical = 10.dp),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            MyText(
                text = reciterName,
                modifier = Modifier.padding(vertical = 10.dp),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProgressSpace(
    progress: String,
    duration: String,
    playbackState: Int,
    areControlsEnabled: Boolean,
    onSliderChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onPreviousTrackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextTrackClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth(0.95f)
            .clip(RoundedCornerShape(10))
            .background(AppTheme.colors.weakPrimary),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyRow(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            MyText(
                text = progress,
                modifier = Modifier.padding(10.dp),
                fontSize = 19.sp
            )

            MySlider(
                value = progress.toFloat(),
                valueRange = 0F..duration.toFloat(),
                modifier = Modifier.fillMaxWidth(0.7F),
                enabled = areControlsEnabled,
                onValueChange = onSliderChange,
                onValueChangeFinished = onSliderChangeFinished
            )

            MyText(
                text = duration,
                modifier = Modifier.padding(10.dp),
                fontSize = 19.sp
            )
        }

        MyRow(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 5.dp)
        ) {
            MyIconButton(
                iconId = R.drawable.ic_previous_track,
                description = stringResource(R.string.previous_track_btn_description),
                isEnabled = areControlsEnabled,
                size = 40.dp,
                innerPadding = 20.dp,
                tint = AppTheme.colors.accent,
                onClick = onPreviousTrackClick
            )

            MyIconPlayerBtn(
                state = playbackState,
                modifier = Modifier.padding(10.dp),
                enabled = areControlsEnabled,
                playIcon = R.drawable.ic_circle_play,
                pauseIcon = R.drawable.ic_circle_pause,
                tint = AppTheme.colors.accent,
                onClick = onPlayPauseClick
            )

            MyIconButton(
                iconId = R.drawable.ic_next_track,
                description = stringResource(R.string.next_track_btn_description),
                isEnabled = areControlsEnabled,
                size = 40.dp,
                innerPadding = 20.dp,
                tint = AppTheme.colors.accent,
                onClick = onNextTrackClick
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BottomBar(
    repeatMode: Int,
    shuffleMode: Int,
    downloadState: DownloadState,
    onRepeatClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onDownloadClick: () -> Unit,
) {
    BottomAppBar(
        backgroundColor = AppTheme.colors.primary
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MyIconButton(
                iconId = R.drawable.ic_repeat,
                description = stringResource(R.string.repeat_description),
                innerPadding = 10.dp,
                tint =
                    if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) AppTheme.colors.secondary
                    else AppTheme.colors.onPrimary,
                onClick = onRepeatClick
            )

            MyDownloadBtn(
                state = downloadState,
                onClick = onDownloadClick,
            )

            MyIconButton(
                iconId = R.drawable.ic_shuffle,
                description = stringResource(R.string.shuffle_description),
                innerPadding = 10.dp,
                tint =
                    if (shuffleMode == SHUFFLE_MODE_ALL) AppTheme.colors.secondary
                    else AppTheme.colors.onPrimary,
                onClick = onShuffleClick
            )
        }
    }
}