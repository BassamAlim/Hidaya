package bassamalim.hidaya.features.tv.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.Globals
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.ParentColumn
import bassamalim.hidaya.core.utils.ActivityUtils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.launch

@Composable
fun TvScreen(viewModel: TvViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    if (state.isLoading) return

    MyScaffold(title = stringResource(R.string.tv_channels)) { padding ->
        ParentColumn(Modifier.padding(padding)) {
            YoutubeScreen(
                language = viewModel.language,
                onInitializationSuccess = viewModel::onInitializationSuccess,
                snackbarHostState = snackbarHostState
            )

            MyColumn(
                modifier = Modifier.weight(1f),
                arrangement = Arrangement.Center
            ) {
                ChannelButton(
                    text = stringResource(R.string.quran_channel),
                    painter = painterResource(R.mipmap.ic_quran_channel),
                    description = stringResource(R.string.quran_channel),
                    onClick = viewModel::onQuranChannelClick
                )

                Spacer(Modifier.height(48.dp))

                ChannelButton(
                    text = stringResource(R.string.sunnah_channel),
                    painter = painterResource(R.mipmap.ic_sunnah_channel),
                    description = stringResource(R.string.quran_channel),
                    onClick = viewModel::onSunnahChannelClick
                )
            }
        }
    }

    KeepScreenOn()
}

@Composable
fun YoutubeScreen(
    language: Language,
    onInitializationSuccess: (YouTubePlayer) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val playbackFailedMessage = stringResource(R.string.playback_failed)

    // a fix because locale changes when displaying YouTubePlayerView for some reason
    DisposableEffect(Unit) {
        ActivityUtils.onActivityCreateSetLocale(context = context, language = language)
        onDispose {}
    }

    AndroidView(
        factory = {
            YouTubePlayerView(it).apply {
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        super.onReady(youTubePlayer)
                        onInitializationSuccess(youTubePlayer)
                    }

                    override fun onError(
                        youTubePlayer: YouTubePlayer,
                        error: PlayerConstants.PlayerError
                    ) {
                        super.onError(youTubePlayer, error)
                        Log.e(Globals.TAG, java.lang.String.valueOf(error))
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(playbackFailedMessage)
                        }
                    }
                })
            }
        }
    )
}

@Composable
private fun ChannelButton(
    text: String,
    painter: Painter,
    description: String,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.7f),
        shape = RoundedCornerShape(10.dp)
    ) {
        MyColumn(Modifier.padding(6.dp)) {
            Image(
                painter = painter,
                contentDescription = description,
                contentScale = ContentScale.None,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )

            Spacer(Modifier.height(6.dp))

            MyText(text = text, fontSize = 24.sp)
        }
    }
}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose { currentView.keepScreenOn = false }
    }
}