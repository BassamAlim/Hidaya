package bassamalim.hidaya.features.recitations.player.ui

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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
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
import bassamalim.hidaya.core.ui.components.MyDownloadButton
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyIconPlayerBtn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySlider
import bassamalim.hidaya.core.ui.components.MyText

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecitationPlayerScreen(viewModel: RecitationPlayerViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

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
            Modifier
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
    onNextTrackClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth(0.95f)
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyRow(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            MyText(
                text = progressText,
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
                text = durationText,
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
                tint = MaterialTheme.colorScheme.primary,
                onClick = onPreviousTrackClick
            )

            MyIconPlayerBtn(
                state = playbackState,
                modifier = Modifier.padding(10.dp),
                enabled = areControlsEnabled,
                playIcon = R.drawable.ic_circle_play,
                pauseIcon = R.drawable.ic_circle_pause,
                tint = MaterialTheme.colorScheme.primary,
                onClick = onPlayPauseClick
            )

            MyIconButton(
                iconId = R.drawable.ic_next_track,
                description = stringResource(R.string.next_track_btn_description),
                isEnabled = areControlsEnabled,
                size = 40.dp,
                innerPadding = 20.dp,
                tint = MaterialTheme.colorScheme.primary,
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
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MyIconButton(
                iconId = R.drawable.ic_repeat,
                description = stringResource(R.string.repeat_description),
                innerPadding = 10.dp,
                tint =
                    if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onPrimary,
                onClick = { onRepeatClick(repeatMode) }
            )

            MyDownloadButton(
                state = downloadState,
                onClick = onDownloadClick,
            )

            MyIconButton(
                iconId = R.drawable.ic_shuffle,
                description = stringResource(R.string.shuffle_description),
                innerPadding = 10.dp,
                tint =
                    if (shuffleMode == SHUFFLE_MODE_ALL) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onPrimary,
                onClick = { onShuffleClick(shuffleMode) }
            )
        }
    }
}