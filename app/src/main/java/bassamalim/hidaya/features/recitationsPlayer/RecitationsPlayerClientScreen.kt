package bassamalim.hidaya.features.recitationsPlayer

import android.app.Activity
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
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
fun TelawatClientUI(
    vm: RecitationsPlayerClientViewModel
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

    DisposableEffect(key1 = vm) {
        vm.onStart(activity)
        onDispose { vm.onStop() }
    }

    MyScaffold(
        title = stringResource(R.string.recitations),
        bottomBar = { BottomBar(vm, st) },
        onBack = { vm.onBackPressed() }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .border(
                        width = 2.dp,
                        shape = RoundedCornerShape(10),
                        color = AppTheme.colors.accent
                    )
            ) {
                Column(
                    Modifier
                        .padding(vertical = 25.dp, horizontal = 75.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MyText(
                        text = st.suraName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    MyText(
                        text = st.versionName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    MyText(
                        text = st.reciterName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }

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
                        text = st.progress,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 19.sp
                    )

                    MySlider(
                        value = vm.progress.toFloat(),
                        valueRange = 0F..vm.duration.toFloat(),
                        modifier = Modifier.fillMaxWidth(0.7F),
                        enabled = st.controlsEnabled,
                        onValueChangeFinished = { vm.onSliderChangeFinished() },
                        onValueChange = { progress -> vm.onSliderChange(progress) }
                    )

                    MyText(
                        text = st.duration,
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
                        isEnabled = st.controlsEnabled,
                        size = 40.dp,
                        innerPadding = 20.dp,
                        tint = AppTheme.colors.accent,
                        onClick = { vm.onPrevClk() }
                    )

                    MyIconPlayerBtn(
                        state = st.btnState,
                        modifier = Modifier.padding(10.dp),
                        enabled = st.controlsEnabled,
                        playIcon = R.drawable.ic_circle_play,
                        pauseIcon = R.drawable.ic_circle_pause,
                        tint = AppTheme.colors.accent,
                        onClick = { vm.onPlayPauseClk() }
                    )

                    MyIconButton(
                        iconId = R.drawable.ic_next_track,
                        description = stringResource(R.string.next_track_btn_description),
                        isEnabled = st.controlsEnabled,
                        size = 40.dp,
                        innerPadding = 20.dp,
                        tint = AppTheme.colors.accent,
                        onClick = { vm.onNextClk() }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BottomBar(viewModel: RecitationsPlayerClientViewModel, state: RecitationsPlayerClientState) {
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
                    if (state.repeat == REPEAT_MODE_ONE) AppTheme.colors.secondary
                    else AppTheme.colors.onPrimary,
                onClick = { viewModel.onRepeatClk() }
            )

            MyDownloadBtn(
                state = state.downloadState,
                path = "${"/Telawat/${viewModel.reciterId}/${viewModel.versionId}/"}${viewModel.suraIdx}.mp3",
                innerPadding = 10.dp,
                tint =
                    if (state.downloadState == DownloadState.Downloaded) AppTheme.colors.secondary
                    else AppTheme.colors.onPrimary,
                deleted = { viewModel.onDeleteClk() },
                download = { viewModel.onDownloadClk() }
            )

            MyIconButton(
                iconId = R.drawable.ic_shuffle,
                description = stringResource(R.string.shuffle_description),
                innerPadding = 10.dp,
                tint =
                    if (state.shuffle == SHUFFLE_MODE_ALL) AppTheme.colors.secondary
                    else AppTheme.colors.onPrimary,
                onClick = { viewModel.onShuffleClk() }
            )
        }
    }
}