package bassamalim.hidaya.features.qibla.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MultiDrawableImage
import bassamalim.hidaya.core.ui.components.MyDialog
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.Negative
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size


@Composable
fun QiblaScreen(viewModel: QiblaViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }

    MyScaffold(title = stringResource(R.string.qibla)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 20.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!state.error) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.select),
                    modifier = Modifier.alpha(if (state.isOnPoint) 1F else 0F),
                    tint = Color.Green
                )

                QiblaGraphics(
                    compassAngle = state.compassAngle,
                    qiblaAngle = state.qiblaAngle
                )

                DirectionAccuracyInfoArea(
                    accuracy = state.accuracy,
                    onAccuracyIndicatorClick = viewModel::onAccuracyIndicatorClick
                )

                MyText(
                    text = String.format(
                        stringResource(R.string.distance_to_kaaba),
                        "${state.distanceToKaaba} ${stringResource(R.string.distance_unit)}"
                    ),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            else {
                MyText(
                    text = stringResource(state.errorMassageResId),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            CalibrationDialog(
                calibrationDialogShown = state.calibrationDialogShown,
                onCalibrationDialogDismiss = viewModel::onCalibrationDialogDismiss
            )
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
                textColor = Negative
            )
        }
    }
}

@Composable
private fun QiblaGraphics(compassAngle: Float, qiblaAngle: Float) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // compass image
        MultiDrawableImage(
            drawables = listOf(
                R.drawable.compass_primary to MaterialTheme.colorScheme.primary,
                R.drawable.compass_on_surface to MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .rotate(compassAngle)
                .padding(horizontal = 10.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.qibla_pointer),
            contentDescription = "",
            modifier = Modifier
                .rotate(qiblaAngle)
                .padding(bottom = 26.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        Image(
            painter = painterResource(id = R.drawable.ic_qibla_kaaba),
            contentDescription = ""
        )
    }
}

@Composable
private fun DirectionAccuracyInfoArea(
    accuracy: Int,
    onAccuracyIndicatorClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        MyText(
            text = when (accuracy) {
                2 -> stringResource(R.string.medium_accuracy_text)
                0, 1 -> stringResource(R.string.low_accuracy_text)
                else -> stringResource(R.string.high_accuracy_text)
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        when (accuracy) {
            0, 1 -> {
                MyIconButton(
                    imageVector = Icons.Outlined.Info,
                    description = stringResource(R.string.accuracy_indicator_description),
                    contentColor = Negative,
                    onClick = onAccuracyIndicatorClick
                )
            }
            else -> {
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (accuracy == 2) Color(0xFF9FAA17)
                            else Color(0xFF1C8818)
                        )
                )
            }
        }
    }
}

@Composable
private fun getImageLoader(context: Context) =
    ImageLoader.Builder(context).components {
        if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
        else add(GifDecoder.Factory())
    }.build()

@Composable
private fun getImageRequest(context: Context) =
    ImageRequest.Builder(context)
    .data(R.drawable.compass_calibration)
    .apply(block = { size(Size.ORIGINAL) }).build()