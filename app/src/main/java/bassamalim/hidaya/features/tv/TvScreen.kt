package bassamalim.hidaya.features.tv

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import bassamalim.hidaya.R
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.ui.components.MyColumn
import bassamalim.hidaya.core.ui.components.MyHorizontalButton
import bassamalim.hidaya.core.ui.components.MyParentColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun TvUI(
    vm: TvVM
) {
    val ctx = LocalContext.current

    KeepScreenOn()

    MyScaffold(stringResource(R.string.tv_channels)) {
        MyParentColumn(
            Modifier.padding(it)
        ) {
            YoutubeScreen(ctx, vm)

            MyColumn(
                modifier = Modifier.padding(top = 100.dp)
            ) {
                MyHorizontalButton(
                    text = stringResource(R.string.quran_channel),
                    icon = {
                        Image(
                            painter = painterResource(R.mipmap.ic_quran_channel),
                            contentDescription = stringResource(R.string.quran_channel)
                        )
                    },
                    modifier = Modifier.padding(bottom = 50.dp)
                ) {
                    vm.onQuranChannelClk()
                }

                MyHorizontalButton(
                    text = stringResource(R.string.sunnah_channel),
                    icon = {
                        Image(
                            painter = painterResource(R.mipmap.ic_sunnah_channel),
                            contentDescription = stringResource(R.string.quran_channel)
                        )
                    }
                ) {
                    vm.onSunnahChannelClk()
                }
            }
        }
    }
}

@Composable
fun YoutubeScreen(
    ctx: Context,
    vm: TvVM
) {
    AndroidView(factory = {
        val view = YouTubePlayerView(it)
        view.addYouTubePlayerListener(
            object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    super.onReady(youTubePlayer)

                    vm.onInitializationSuccess(youTubePlayer)
                }

                override fun onError(
                    youTubePlayer: YouTubePlayer,
                    error: PlayerConstants.PlayerError
                ) {
                    super.onError(youTubePlayer, error)

                    Log.e(Global.TAG, java.lang.String.valueOf(error))

                    Toast.makeText(
                        ctx,
                        ctx.getString(R.string.playback_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        view
    })
}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}