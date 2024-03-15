package bassamalim.hidaya.features.radio

import android.app.Activity
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyCircularProgressIndicator
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RadioClientUI(
    vm: RadioClientVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

    DisposableEffect(key1 = vm) {
        vm.onStart(activity)
        onDispose { vm.onStop() }
    }

    MyScaffold(stringResource(R.string.quran_radio)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = stringResource(R.string.holy_quran_radio),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 50.dp)
            )

            PlayPauseBtn(vm, st)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PlayPauseBtn(
    vm: RadioClientVM,
    st: RadioClientState
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .clickable { vm.onPlayPause() }
    ) {
        AnimatedContent(
            targetState = st.btnState,
            label = "",
            transitionSpec = {
                scaleIn(animationSpec = tween(durationMillis = 200)) togetherWith
                        scaleOut(animationSpec = tween(durationMillis = 200))
            }
        ) { state ->
            if (state == PlaybackStateCompat.STATE_NONE
                || state == PlaybackStateCompat.STATE_CONNECTING
                || state == PlaybackStateCompat.STATE_BUFFERING)
                MyCircularProgressIndicator()
            else {
                Image(
                    painter = painterResource(
                        if (state == PlaybackStateCompat.STATE_PLAYING) R.drawable.ic_radio_pause
                        else R.drawable.ic_radio_play
                    ),
                    contentDescription = stringResource(R.string.play_pause_btn_description),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}