package bassamalim.hidaya.view

import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.state.QiblaState
import bassamalim.hidaya.ui.components.MyDialog
import bassamalim.hidaya.ui.components.MyIconBtn
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.viewmodel.QiblaVM
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size

@Composable
fun QiblaUI(
    vm: QiblaVM = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose { vm.onStop() }
    }

    MyScaffold(stringResource(R.string.qibla)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 20.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!state.error) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "",
                    tint = Color.Green,
                    modifier = Modifier.alpha(if (state.isOnPoint) 1F else 0F)
                )

                Box(
                    Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.compass),
                        contentDescription = "",
                        modifier = Modifier
                            .rotate(state.compassAngle)
                            .padding(horizontal = 10.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.qibla_pointer),
                        contentDescription = "",
                        modifier = Modifier
                            .rotate(state.qiblaAngle)
                            .padding(bottom = 26.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_qibla_kaaba),
                        contentDescription = ""
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    MyText(
                        text = when (state.accuracy) {
                            2 -> {
                                stringResource(R.string.medium_accuracy_text)
                            }
                            0, 1 -> {
                                stringResource(R.string.low_accuracy_text)
                            }
                            else -> {
                                stringResource(R.string.high_accuracy_text)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    when (state.accuracy) {
                        0, 1 -> {
                            MyIconBtn(
                                iconId = R.drawable.ic_warning,
                                description = stringResource(R.string.accuracy_indicator_description),
                                tint = Color(0xFFE2574C)
                            ) {
                                vm.onAccuracyIndicatorClick()
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (state.accuracy == 2) Color(0xFF9FAA17)
                                        else Color(0xFF1C8818)
                                    )
                            )
                        }
                    }
                }

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

            CalibrationDialog(context, vm, state)
        }
    }
}

@Composable
private fun CalibrationDialog(context: Context, viewModel: QiblaVM, state: QiblaState) {
    MyDialog(
        shown = state.calibrationDialogShown,
        onDismiss = { viewModel.onCalibrationDialogDismiss() },
    ) {
        Column(
            Modifier.padding(vertical = 20.dp, horizontal = 30.dp)
        ) {
            val imageLoader = ImageLoader.Builder(context).components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }.build()

            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(R.drawable.compass_calibration)
                        .apply(block = { size(Size.ORIGINAL) }).build(),
                    imageLoader = imageLoader
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            MyText(
                stringResource(R.string.qibla_warning),
                textColor = Color.Red
            )
        }
    }
}