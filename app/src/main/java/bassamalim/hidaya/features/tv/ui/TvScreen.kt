package bassamalim.hidaya.features.tv.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyHorizontalButton
import bassamalim.hidaya.core.ui.components.MyScaffold
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
        ParentColumn(
            Modifier.padding(padding)
        ) {
            YoutubeScreen(
                language = viewModel.language,
                onInitializationSuccess = viewModel::onInitializationSuccess,
                snackbarHostState = snackbarHostState
            )

            MyColumn(
                Modifier.padding(top = 100.dp)
            ) {
                MyHorizontalButton(
                    text = stringResource(R.string.quran_channel),
                    icon = {
                        Image(
                            painter = painterResource(R.mipmap.ic_quran_channel),
                            contentDescription = stringResource(R.string.quran_channel)
                        )
                    },
                    modifier = Modifier.padding(bottom = 50.dp),
                    onClick = viewModel::onQuranChannelClick
                )

                MyHorizontalButton(
                    text = stringResource(R.string.sunnah_channel),
                    icon = {
                        Image(
                            painter = painterResource(R.mipmap.ic_sunnah_channel),
                            contentDescription = stringResource(R.string.quran_channel)
                        )
                    },
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
                        Log.e(Global.TAG, java.lang.String.valueOf(error))
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
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose { currentView.keepScreenOn = false }
    }
}