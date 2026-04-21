package bassamalim.hidaya.features.radio

import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val TWO_PI = (2 * PI).toFloat()

// Dot positions expressed as (angle°, distance-ratio of ring radius)
private val DOT_POSITIONS = listOf(
    18f to 0.74f,  145f to 0.88f,  220f to 0.76f,  310f to 0.91f,
    72f to 0.95f,  168f to 0.80f,  258f to 0.70f,  340f to 0.86f
)
private val DOT_RADII_DP = listOf(1.8f, 1.2f, 1.5f, 1.0f, 1.8f, 1.3f, 1.0f, 1.5f)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RadioClientScreen(viewModel: RadioClientViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current!!

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(activity)
        onDispose(viewModel::onStop)
    }

    MyScaffold(title = stringResource(R.string.quran_radio)) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(52.dp))

                MyText(
                    text = stringResource(R.string.holy_quran_radio),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(Modifier.weight(0.5f))

                OrbitalButton(
                    state = state.btnState,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = viewModel::onPlayPauseClick
                )

                Spacer(Modifier.weight(1.1f))
            }

            BottomWaves(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-140).dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun OrbitalButton(
    state: Int,
    color: Color,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "orbital")

    val rot1 by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "r1"
    )
    val rot2 by transition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "r2"
    )
    val rot3 by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "r3"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(250.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val ringR = size.minDimension / 2f * 0.9f

            // ryRatio ~0.55-0.65 looks like a circle tilted ~50°, not a squashed oval
            listOf(
                Triple(rot1,        1.00f, 0.62f),
                Triple(rot2 + 60f,  0.96f, 0.55f),
                Triple(rot3 + 30f,  0.92f, 0.60f)
            ).forEach { (rotation, rxRatio, ryRatio) ->
                withTransform({ rotate(rotation, Offset(cx, cy)) }) {
                    val rx = ringR * rxRatio
                    val ry = ringR * ryRatio
                    val top = Offset(cx - rx, cy - ry)
                    val sz  = Size(rx * 2f, ry * 2f)
                    drawOval(
                        color = color.copy(alpha = 0.04f),
                        topLeft = top, size = sz,
                        style = Stroke(width = 5.dp.toPx())
                    )
                    drawOval(
                        color = color.copy(alpha = 0.18f),
                        topLeft = top, size = sz,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // Scattered particle dots in the orbital band
            DOT_POSITIONS.forEachIndexed { i, (angleDeg, distRatio) ->
                val rad = angleDeg * PI.toFloat() / 180f
                val dist = ringR * distRatio
                drawCircle(
                    color = color.copy(alpha = 0.22f),
                    radius = DOT_RADII_DP[i].dp.toPx(),
                    center = Offset(cx + cos(rad) * dist, cy + sin(rad) * dist)
                )
            }
        }

        // Play / pause icon
        AnimatedContent(
            targetState = state,
            label = "btn",
            transitionSpec = {
                scaleIn(tween(200)) togetherWith scaleOut(tween(200))
            }
        ) { btnState ->
            when (btnState) {
                PlaybackStateCompat.STATE_NONE,
                PlaybackStateCompat.STATE_CONNECTING,
                PlaybackStateCompat.STATE_BUFFERING -> MyCircularProgressIndicator()
                else -> {
                    val containerRes =
                        if (btnState == PlaybackStateCompat.STATE_PLAYING)
                            R.drawable.ic_radio_pause_container
                        else R.drawable.ic_radio_play_container
                    val primaryRes =
                        if (btnState == PlaybackStateCompat.STATE_PLAYING)
                            R.drawable.ic_radio_pause_primary
                        else R.drawable.ic_radio_play_primary

                    MultiDrawableImage(
                        drawables = listOf(
                            containerRes to MaterialTheme.colorScheme.primaryContainer,
                            primaryRes to MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onClick),
                        innerModifier = Modifier.fillMaxSize(),
                        contentDescription = stringResource(R.string.play_pause_btn_description)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomWaves(
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "bwaves")
    val phase1 by transition.animateFloat(
        initialValue = 0f, targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "bp1"
    )
    val phase2 by transition.animateFloat(
        initialValue = TWO_PI, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart),
        label = "bp2"
    )
    val phase3 by transition.animateFloat(
        initialValue = 0f, targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "bp3"
    )

    Canvas(modifier = modifier) {
        val cy = size.height / 2f
        val amp = size.height * 0.28f
        val freq = 2.5f

        drawSineLine(color.copy(alpha = 0.55f), cy, phase1,          amp,        freq, 2.dp.toPx())
        drawSineLine(color.copy(alpha = 0.35f), cy, phase2 + 0.8f,   amp * 0.8f, freq, 1.5.dp.toPx())
        drawSineLine(color.copy(alpha = 0.25f), cy, phase3 + 1.6f,   amp * 0.9f, freq, 1.dp.toPx())
    }
}

private fun DrawScope.drawSineLine(
    color: Color,
    yCenter: Float,
    phase: Float,
    amplitude: Float,
    waveCount: Float,
    strokeWidth: Float
) {
    val path = Path()
    val steps = 200

    path.moveTo(0f, yCenter + amplitude * sin(phase))
    for (i in 1..steps) {
        val x = size.width * i / steps
        val y = yCenter + amplitude * sin(waveCount * TWO_PI * i / steps + phase)
        path.lineTo(x, y)
    }
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}
