package bassamalim.hidaya.features.recitations.player.ui

import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.ui.components.MyDownloadButton
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyIconPlayerButton
import bassamalim.hidaya.core.ui.components.MyProgressSlider
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecitationPlayerScreen(viewModel: RecitationPlayerViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current!!

    if (state.isLoading) return

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(activity)
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InfoSpace(
                suraName = state.suraName,
                narrationName = state.narrationName,
                reciterName = state.reciterName
            )

            ProgressSpace(
                progress = viewModel.progress,
                progressText = state.progress,
                duration = viewModel.duration,
                durationText = state.duration,
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
private fun InfoSpace(suraName: String, narrationName: String, reciterName: String) {
    Box(
        Modifier
            .padding(horizontal = 10.dp)
            .border(
                width = 2.dp,
                shape = RoundedCornerShape(10),
                color = MaterialTheme.colorScheme.primary
            )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 25.dp, horizontal = 75.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MyText(
                text = suraName,
                modifier = Modifier.padding(vertical = 10.dp),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            MyText(
                text = reciterName,
                modifier = Modifier.padding(vertical = 10.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )

            MyText(
                text = narrationName,
                modifier = Modifier.padding(vertical = 10.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ProgressSpace(
    progress: Long,
    progressText: String,
    duration: Long,
    durationText: String,
    playbackState: Int,
    areControlsEnabled: Boolean,
    onSliderChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onPreviousTrackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextTrackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyText(
                text = progressText,
                modifier = Modifier
                    .weight(0.4f)
                    .padding(horizontal = 10.dp),
                fontSize = 19.sp
            )

            MyProgressSlider(
                value = progress.toFloat(),
                valueRange = 0F..duration.toFloat(),
                modifier = Modifier.weight(1f),
                enabled = areControlsEnabled,
                onValueChange = onSliderChange,
                onValueChangeFinished = onSliderChangeFinished
            )

            MyText(
                text = durationText,
                modifier = Modifier
                    .weight(0.4f)
                    .padding(horizontal = 10.dp),
                fontSize = 19.sp
            )
        }

        MyRow(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 16.dp)
        ) {
            MyIconButton(
                iconId = R.drawable.ic_skip_previous,
                description = stringResource(R.string.previous_track_btn_description),
                modifier = Modifier.size(80.dp),
                iconSize = 70.dp,
                enabled = areControlsEnabled,
                contentColor = MaterialTheme.colorScheme.primary,
                onClick = onPreviousTrackClick
            )

            MyIconPlayerButton(
                state = playbackState,
                enabled = areControlsEnabled,
                modifier = Modifier.size(100.dp),
                iconSize = 90.dp,
                tint = MaterialTheme.colorScheme.primary,
                onClick = onPlayPauseClick
            )

            MyIconButton(
                iconId = R.drawable.ic_skip_next,
                description = stringResource(R.string.next_track_btn_description),
                enabled = areControlsEnabled,
                modifier = Modifier.size(80.dp),
                iconSize = 70.dp,
                contentColor = MaterialTheme.colorScheme.primary,
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
    onRepeatClick: (Int) -> Unit,
    onShuffleClick: (Int) -> Unit,
    onDownloadClick: () -> Unit,
) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MyIconButton(
                imageVector = Icons.Default.Repeat,
                description = stringResource(R.string.repeat_description),
                onClick = { onRepeatClick(repeatMode) },
                iconModifier = Modifier.size(30.dp),
                contentColor =
                    if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.onPrimary
            )

            MyDownloadButton(
                state = downloadState,
                onClick = onDownloadClick,
                iconSize = 30.dp,
                contentColor = MaterialTheme.colorScheme.onSurface
            )

            MyIconButton(
                imageVector = Icons.Default.Shuffle,
                description = stringResource(R.string.shuffle_description),
                onClick = { onShuffleClick(shuffleMode) },
                iconModifier = Modifier.size(30.dp),
                contentColor =
                    if (shuffleMode == SHUFFLE_MODE_ALL) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}