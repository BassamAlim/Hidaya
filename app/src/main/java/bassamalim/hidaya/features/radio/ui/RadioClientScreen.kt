package bassamalim.hidaya.features.radio.ui

import android.app.Activity
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import bassamalim.hidaya.core.ui.components.MultiDrawableImage
import bassamalim.hidaya.core.ui.components.MyCircularProgressIndicator
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RadioClientScreen(viewModel: RadioClientViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(activity)
        onDispose(viewModel::onStop)
    }

    MyScaffold(title = stringResource(R.string.quran_radio)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = stringResource(R.string.holy_quran_radio),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 50.dp)
            )

            PlayPauseBtn(
                state = state.btnState,
                onClick = viewModel::onPlayPauseClick
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PlayPauseBtn(
    state: Int,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        AnimatedContent(
            targetState = state,
            label = "",
            transitionSpec = {
                scaleIn(animationSpec = tween(durationMillis = 200)) togetherWith
                        scaleOut(animationSpec = tween(durationMillis = 200))
            }
        ) { state ->
            when (state) {
                PlaybackStateCompat.STATE_NONE,
                PlaybackStateCompat.STATE_CONNECTING,
                PlaybackStateCompat.STATE_BUFFERING -> {
                    MyCircularProgressIndicator()
                }
                else -> {
                    val containerResourceId =
                        if (state == PlaybackStateCompat.STATE_PLAYING) R.drawable.ic_radio_pause_container
                        else R.drawable.ic_radio_play_container
                    val primaryResourceId =
                        if (state == PlaybackStateCompat.STATE_PLAYING) R.drawable.ic_radio_pause_primary
                        else R.drawable.ic_radio_play_primary

                    // play/pause button
                    MultiDrawableImage(
                        drawables = listOf(
                            containerResourceId to MaterialTheme.colorScheme.primaryContainer,
                            primaryResourceId to MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(10.dp),
                        contentDescription = stringResource(R.string.play_pause_btn_description)
                    )
                }
            }
        }
    }
}