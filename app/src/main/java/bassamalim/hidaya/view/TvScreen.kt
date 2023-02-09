package bassamalim.hidaya.view

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.hilt.navigation.compose.hiltViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyCircularProgressIndicator
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.viewmodel.TvVM
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragmentXKt

@Composable
fun TvUI(
    vm: TvVM = hiltViewModel()
) {
    val ctx = LocalContext.current
    val st by vm.uiState.collectAsState()

    KeepScreenOn()

    MyScaffold(stringResource(R.string.tv_channels)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Box {
                YoutubeScreen(ctx, vm)

                if (st.isLoading) {
                    MyCircularProgressIndicator()
                }
            }

            MyButton(text = stringResource(R.string.quran_channel)) {
                vm.onQuranChannelClk()
            }

            MyButton(text = stringResource(R.string.sunnah_channel)) {
                vm.onSunnahChannelClk()
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