package bassamalim.hidaya.view

import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.enum.DownloadState
import bassamalim.hidaya.state.TelawatClientState
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.viewmodel.TelawatClientVM

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TelawatClientUI(
    navController: NavController = rememberNavController(),
    viewModel: TelawatClientVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }

    MyScaffold(
        title = stringResource(R.string.recitations),
        bottomBar = { BottomBar(viewModel, state) },
        onBack = { viewModel.onBackPressed(navController) }
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
                        text = state.suraName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    MyText(
                        text = state.versionName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    MyText(
                        text = state.reciterName,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.weakPrimary),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MyText(
                    text = state.progress,
                    modifier = Modifier.padding(10.dp)
                )

                MySlider(
                    value = state.progress.toFloat(),
                    valueRange = 0F..state.duration.toFloat(),
                    modifier = Modifier.fillMaxWidth(0.7F),
                    enabled = state.controlsEnabled,
                    onValueChangeFinished = { viewModel.onSliderChangeFinished() },
                    onValueChange = { progress -> viewModel.onSliderChange(progress) }
                )

                MyText(
                    text = state.duration,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.weakPrimary),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MyImageButton(
                    imageResId = R.drawable.ic_player_previous,
                    description = stringResource(R.string.previous_day_button_description),
                    enabled = state.controlsEnabled
                ) {
                    viewModel.onPrevClk()
                }

                MyImageButton(
                    imageResId = R.drawable.ic_backward,
                    description = stringResource(R.string.rewind_btn_description),
                    enabled = state.controlsEnabled
                ) {
                    viewModel.onRewindClk()
                }

                MyPlayerBtn(
                    state = state.btnState,
                    enabled = state.controlsEnabled,
                ) {
                    viewModel.onPlayPauseClk()
                }

                MyImageButton(
                    imageResId = R.drawable.ic_forward,
                    description = stringResource(R.string.fast_forward_btn_description),
                    enabled = state.controlsEnabled
                ) {
                    viewModel.onFastForwardClk()
                }

                MyImageButton(
                    imageResId = R.drawable.ic_player_next,
                    description = stringResource(R.string.next_track_btn_description),
                    enabled = state.controlsEnabled
                ) {
                    viewModel.onNextClk()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BottomBar(viewModel: TelawatClientVM, state: TelawatClientState) {
    BottomAppBar(
        backgroundColor = AppTheme.colors.primary
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MyIconBtn(
                iconId = R.drawable.ic_repeat,
                description = stringResource(R.string.repeat_description),
                tint =
                    if (state.repeat == REPEAT_MODE_ONE) AppTheme.colors.secondary
                    else AppTheme.colors.onPrimary
            ) {
                viewModel.onRepeatClk()
            }

            MyDownloadBtn(
                state = state.downloadState,
                path = "${"/Telawat/${viewModel.reciterId}/${viewModel.versionId}/"}${viewModel.suraIdx}.mp3",
                size = 28.dp,
                tint =
                if (state.downloadState == DownloadState.Downloaded)
                    AppTheme.colors.secondary
                else AppTheme.colors.onPrimary,
                deleted = { viewModel.onDelete() }
            ) {
                viewModel.onDownloadClk()
            }

            MyIconBtn(
                iconId = R.drawable.ic_shuffle,
                description = stringResource(R.string.shuffle_description),
                tint =
                    if (state.shuffle == SHUFFLE_MODE_ALL) AppTheme.colors.secondary
                    else AppTheme.colors.onPrimary
            ) {
                viewModel.onShuffleClk()
            }
        }
    }
}