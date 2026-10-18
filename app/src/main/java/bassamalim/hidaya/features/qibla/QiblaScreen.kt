package bassamalim.hidaya.features.qibla

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyCard
import bassamalim.hidaya.core.ui.components.MyDialog
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.Negative
import bassamalim.hidaya.core.ui.theme.appTypography
import bassamalim.hidaya.core.ui.theme.dimensions
import coil3.ImageLoader
import coil3.compose.rememberAsyncImagePainter
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import coil3.size.Size
import kotlin.math.cos
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun QiblaScreen(viewModel: QiblaViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }

    val view = LocalView.current
    LaunchedEffect(state.isOnPoint) {
        // Respects the system touch-feedback setting
        if (state.isOnPoint) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    MyScaffold(title = stringResource(R.string.qibla)) { padding ->
        val errorMessageResId = state.errorMessageResId
        if (errorMessageResId != null) {
            ErrorState(
                messageResId = errorMessageResId,
                modifier = Modifier.padding(padding),
                onSetLocationClick = viewModel::onSetLocationClick
            )
        }
        else {
            QiblaContent(
                state = state,
                modifier = Modifier.padding(padding),
                onAccuracyClick = viewModel::onAccuracyIndicatorClick
            )
        }

        CalibrationDialog(
            calibrationDialogShown = state.calibrationDialogShown,
            onCalibrationDialogDismiss = viewModel::onCalibrationDialogDismiss
        )
    }
}

@Composable
private fun QiblaContent(
    state: QiblaUiState,
    modifier: Modifier = Modifier,
    onAccuracyClick: () -> Unit
) {
    val dims = MaterialTheme.dimensions

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dims.screenPaddingHorizontal, vertical = dims.spaceXl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DirectionStatus(
            isOnPoint = state.isOnPoint,
            offsetDegrees = state.offsetDegrees,
            offsetText = state.offsetText
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            QiblaGraphics(
                compassAngle = state.compassAngle,
                qiblaAngle = state.qiblaAngle,
                isOnPoint = state.isOnPoint
            )
        }

        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(dims.spaceMd)
        ) {
            InfoTile(
                label = stringResource(R.string.kaaba_distance),
                value = "${state.distanceToKaaba} ${stringResource(R.string.distance_unit)}",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            AccuracyTile(
                accuracy = state.accuracy,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = onAccuracyClick
            )
        }
    }
}

@Composable
private fun DirectionStatus(isOnPoint: Boolean, offsetDegrees: Int, offsetText: String) {
    val dims = MaterialTheme.dimensions
    val containerColor by animateColorAsState(
        targetValue =
            if (isOnPoint) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "direction status container"
    )
    val contentColor by animateColorAsState(
        targetValue =
            if (isOnPoint) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
        label = "direction status content"
    )

    Surface(
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dims.spaceXl, vertical = dims.spaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Physical left/right, so these icons are deliberately not auto-mirrored
            Icon(
                imageVector = when {
                    isOnPoint -> Icons.Default.CheckCircle
                    offsetDegrees > 0 -> Icons.Default.TurnRight
                    else -> Icons.Default.TurnLeft
                },
                contentDescription = null,
                modifier = Modifier.size(dims.iconMd)
            )

            Spacer(Modifier.width(dims.spaceSm))

            Text(
                text = when {
                    isOnPoint -> stringResource(R.string.facing_qibla)
                    offsetDegrees > 0 -> stringResource(R.string.turn_right, offsetText)
                    else -> stringResource(R.string.turn_left, offsetText)
                },
                style = MaterialTheme.appTypography.title
            )
        }
    }
}

@Composable
private fun QiblaGraphics(compassAngle: Float, qiblaAngle: Float, isOnPoint: Boolean) {
    val onPoint by animateFloatAsState(
        targetValue = if (isOnPoint) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "on point"
    )
    // Ripples only run while on target, so the dial doesn't redraw every frame otherwise
    val ripple =
        if (isOnPoint) {
            rememberInfiniteTransition(label = "qibla ripple").animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(durationMillis = 2400, easing = LinearEasing)),
                label = "qibla ripple"
            ).value
        }
        else null

    Box(
        modifier = Modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        QiblaDial(
            compassAngle = compassAngle,
            qiblaAngle = qiblaAngle,
            onPoint = onPoint,
            ripple = ripple,
            modifier = Modifier.fillMaxSize()
        )

        Image(
            painter = painterResource(id = R.drawable.ic_qibla_kaaba),
            contentDescription = "",
            modifier = Modifier.fillMaxSize(0.22f)
        )
    }
}

/**
 * Heading-up dial: a fixed mark and a soft beam at the top are where the phone points, the qibla
 * is a glowing orb on the track, and a comet tail between them shows how far and which way to
 * turn. On target, the center lights up and ripples spread out. Quiet upright cardinal labels
 * turn inside the track. Sizes are fractions of the dial, and it's drawn in canvas coordinates,
 * so east stays on the right in RTL.
 *
 * @param onPoint 0..1, animated in and out of the on-target state
 * @param ripple 0..1 looping ripple progress while on target, null otherwise
 */
@Composable
private fun QiblaDial(
    compassAngle: Float,
    qiblaAngle: Float,
    onPoint: Float,
    ripple: Float?,
    modifier: Modifier = Modifier
) {
    val labels = listOf(
        stringResource(R.string.compass_north),
        stringResource(R.string.compass_east),
        stringResource(R.string.compass_south),
        stringResource(R.string.compass_west)
    )
    val labelStyle = MaterialTheme.appTypography.title.copy(fontWeight = FontWeight.Bold)
    val colors = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier) {
        val dial = size.minDimension
        val trackRadius = dial * 0.44f
        val discRadius = dial * 0.22f
        val trackRect = Rect(center = center, radius = trackRadius)
        // Same normalization as the view model: -180..180, positive = turn right
        val offset = ((qiblaAngle % 360) + 540) % 360 - 180
        // The orb eases onto the heading mark as the on-target state animates in
        val orbAngle = offset * (1 - onPoint)

        // Heading beam: a soft wedge of light from the center toward the top
        drawArc(
            brush = Brush.radialGradient(
                0.4f to colors.primary.copy(alpha = 0.22f + 0.18f * onPoint),
                1f to Color.Transparent,
                center = center,
                radius = trackRadius
            ),
            startAngle = -101f,
            sweepAngle = 22f,
            useCenter = true,
            topLeft = trackRect.topLeft,
            size = trackRect.size
        )

        // On-target glow behind the center disc
        if (onPoint > 0f) {
            drawCircle(
                brush = Brush.radialGradient(
                    0.5f to colors.primary.copy(alpha = 0.35f * onPoint),
                    1f to Color.Transparent,
                    center = center,
                    radius = dial * 0.36f
                ),
                radius = dial * 0.36f
            )
        }
        if (ripple != null) {
            for (phase in listOf(0f, 0.5f)) {
                val progress = (ripple + phase) % 1f
                drawCircle(
                    color = colors.primary.copy(alpha = 0.5f * (1 - progress) * onPoint),
                    radius = discRadius + (trackRadius - discRadius) * progress,
                    style = Stroke(width = dial * 0.006f)
                )
            }
        }

        drawCircle(
            color = colors.outlineVariant,
            radius = trackRadius,
            style = Stroke(width = dial * 0.006f)
        )

        // Comet tail from the heading mark to the orb, brightest at the orb. Drawn for a
        // clockwise sweep from 3 o'clock, mirrored for left turns and turned to start at the top.
        val sweep = abs(orbAngle)
        if (sweep > 0.5f) {
            scale(scaleX = if (orbAngle < 0) -1f else 1f, scaleY = 1f, pivot = center) {
                rotate(degrees = -90f, pivot = center) {
                    val tail = Brush.sweepGradient(
                        0f to colors.primary.copy(alpha = 0f),
                        sweep / 360f to colors.primary,
                        // Fades again past the orb, so the tail's round start cap (which wraps
                        // to ~360°) doesn't pick up the bright end color
                        1f to colors.primary.copy(alpha = 0f),
                        center = center
                    )
                    for ((width, alpha) in listOf(0.05f to 0.12f, 0.032f to 0.25f, 0.016f to 1f)) {
                        drawArc(
                            brush = tail,
                            startAngle = 0f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = trackRect.topLeft,
                            size = trackRect.size,
                            alpha = alpha,
                            style = Stroke(width = dial * width, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        // Center disc: lit from above, warming to the primary tones on target
        drawCircle(
            brush = Brush.verticalGradient(
                0f to lerp(colors.surfaceContainerHighest, colors.primaryContainer, onPoint),
                1f to lerp(colors.surfaceContainerLow, colors.primaryContainer.copy(alpha = 0.6f), onPoint),
                startY = center.y - discRadius,
                endY = center.y + discRadius
            ),
            radius = discRadius
        )
        drawCircle(
            color = lerp(colors.outlineVariant, colors.primary, onPoint),
            radius = discRadius,
            style = Stroke(width = dial * 0.004f)
        )

        // Heading mark: fixed, never rotates
        drawLine(
            color = colors.onSurface,
            start = Offset(center.x, center.y - dial * 0.5f),
            end = Offset(center.x, center.y - dial * 0.475f),
            strokeWidth = dial * 0.014f,
            cap = StrokeCap.Round
        )

        val fontSize = (dial * 0.046f).toSp()
        val labelRadius = dial * 0.35f
        labels.forEachIndexed { i, label ->
            val layout = textMeasurer.measure(
                text = label,
                style = labelStyle.copy(
                    color = if (i == 0) colors.onSurface else colors.onSurfaceVariant,
                    fontSize = fontSize
                )
            )
            // Canvas angles: 0 = east, clockwise; north is -90
            val labelRadians = Math.toRadians(-90.0 + 90.0 * i + compassAngle)
            val position = Offset(
                x = center.x + labelRadius * cos(labelRadians).toFloat(),
                y = center.y + labelRadius * sin(labelRadians).toFloat()
            )
            // ponytail: one visual-middle em for both scripts (Tajawal Arabic ~0.23, Latin caps ~0.35)
            val top = position.y - layout.firstBaseline + fontSize.toPx() * 0.3f
            drawText(layout, topLeft = Offset(position.x - layout.size.width / 2, top))
        }

        // Qibla orb: soft glow, solid body, bright core
        val orbRadians = Math.toRadians(-90.0 + orbAngle)
        val orb = Offset(
            x = center.x + trackRadius * cos(orbRadians).toFloat(),
            y = center.y + trackRadius * sin(orbRadians).toFloat()
        )
        val glowRadius = dial * (0.075f + 0.015f * onPoint)
        drawCircle(
            brush = Brush.radialGradient(
                0.3f to colors.primary.copy(alpha = 0.6f),
                1f to Color.Transparent,
                center = orb,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = orb
        )
        drawCircle(color = colors.primary, radius = dial * 0.036f, center = orb)
        drawCircle(color = colors.onPrimary, radius = dial * 0.014f, center = orb)
    }
}

@Composable
private fun InfoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null
) {
    val dims = MaterialTheme.dimensions

    MyCard(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(dims.radiusLg)
    ) {
        Text(
            text = label,
            style = MaterialTheme.appTypography.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(dims.spaceXs))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(dims.spaceSm))
            }

            Text(text = value, style = MaterialTheme.appTypography.title)
        }
    }
}

@Composable
private fun AccuracyTile(accuracy: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    // SensorManager accuracy: 0 unreliable, 1 low, 2 medium, 3 high
    val (valueResId, color) = when (accuracy) {
        0, 1 -> R.string.accuracy_low to MaterialTheme.colorScheme.error
        2 -> R.string.accuracy_moderate to MaterialTheme.colorScheme.tertiary
        else -> R.string.accuracy_good to MaterialTheme.colorScheme.primary
    }

    InfoTile(
        label = stringResource(R.string.accuracy_indicator_description),
        value = stringResource(valueResId),
        modifier = modifier,
        onClick = onClick,
        leading = {
            Box(
                Modifier
                    .size(MaterialTheme.dimensions.spaceMd)
                    .background(color = color, shape = CircleShape)
            )
        }
    )
}

@Composable
private fun ErrorState(
    messageResId: Int,
    modifier: Modifier = Modifier,
    onSetLocationClick: () -> Unit
) {
    val dims = MaterialTheme.dimensions
    val isLocationError = messageResId == R.string.location_permission_for_qibla

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dims.spaceXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isLocationError) Icons.Default.LocationOff else Icons.Default.SensorsOff,
            contentDescription = null,
            modifier = Modifier.size(dims.iconXl * 2),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(dims.spaceLg))

        Text(
            text = stringResource(messageResId),
            style = MaterialTheme.appTypography.title,
            textAlign = TextAlign.Center
        )

        if (isLocationError) {
            Spacer(Modifier.height(dims.spaceXl))

            Button(onClick = onSetLocationClick) {
                Text(
                    text = stringResource(R.string.set_location),
                    style = MaterialTheme.appTypography.button
                )
            }
        }
    }
}

@Composable
private fun CalibrationDialog(
    calibrationDialogShown: Boolean,
    onCalibrationDialogDismiss: () -> Unit
) {
    val context = LocalContext.current

    MyDialog(
        shown = calibrationDialogShown,
        onDismiss = onCalibrationDialogDismiss,
    ) {
        Column(
            Modifier.padding(vertical = 20.dp, horizontal = 30.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = getImageRequest(context),
                    imageLoader = getImageLoader(context)
                ),
                contentDescription = stringResource(R.string.compass_calibration),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            MyText(
                text = stringResource(R.string.qibla_warning),
                color = Negative
            )
        }
    }
}

@Composable
private fun getImageLoader(context: Context) = remember(context) {
    ImageLoader.Builder(context).components {
        if (Build.VERSION.SDK_INT >= 28) add(AnimatedImageDecoder.Factory())
        else add(GifDecoder.Factory())
    }.build()
}

@Composable
private fun getImageRequest(context: Context) = remember(context) {
    ImageRequest.Builder(context)
        .data(R.drawable.compass_calibration)
        .size(Size.ORIGINAL)
        .build()
}
