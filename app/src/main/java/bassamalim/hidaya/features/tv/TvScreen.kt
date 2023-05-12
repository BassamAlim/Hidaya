package bassamalim.hidaya.features.tv

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import bassamalim.hidaya.R
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.ui.components.MyHorizontalButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragmentXKt

@Composable
fun TvUI(
    vm: TvVM
) {
    val ctx = LocalContext.current

    KeepScreenOn()

    MyScaffold(stringResource(R.string.tv_channels)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            YoutubeScreen(ctx, vm)

            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1F),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyHorizontalButton(
                    text = stringResource(R.string.quran_channel),
                    icon = {
                        Image(
                            painter = painterResource(R.mipmap.ic_quran_channel),
                            contentDescription = stringResource(R.string.quran_channel)
                        )
                    },
                    modifier = Modifier.padding(bottom = 20.dp)
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
                    },
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    vm.onSunnahChannelClk()
                }
            }
        }
    }
}

@Composable
fun YoutubeScreen(ctx: Context, viewModel: TvVM) {
    AndroidView(factory = {
        val fragment = YouTubePlayerSupportFragmentXKt().apply {
            initialize(viewModel.apiKey,
                object : YouTubePlayer.OnInitializedListener {
                    override fun onInitializationSuccess(
                        provider: YouTubePlayer.Provider,
                        player: YouTubePlayer,
                        wasRestored: Boolean
                    ) {
                        viewModel.onInitializationSuccess(player)
                    }

                    override fun onInitializationFailure(
                        provider: YouTubePlayer.Provider,
                        result: YouTubeInitializationResult
                    ) {
                        Log.e(Global.TAG, java.lang.String.valueOf(result))

                        Toast.makeText(
                            ctx,
                            getString(R.string.playback_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        (ctx as AppCompatActivity).supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.fragment_container_view_tag, fragment)
        }

        FragmentContainerView(it).apply {
            id = R.id.fragment_container_view_tag
        }
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