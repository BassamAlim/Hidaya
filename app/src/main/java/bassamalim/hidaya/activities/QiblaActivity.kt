package bassamalim.hidaya.activities

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.helpers.Compass
import bassamalim.hidaya.ui.components.MyDialog
import bassamalim.hidaya.ui.components.MyIconBtn
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.LangUtils
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import kotlin.math.*

class QiblaActivity : AppCompatActivity() {

    private val kaabaLat = 21.4224779
    private val kaabaLng = 39.8251832
    private val kaabaLatInRad = Math.toRadians(kaabaLat)
    private var compass: Compass? = null
    private lateinit var location: Location
    private var currentAzimuth = 0F
    private var distance = 0.0
    private var bearing = 0F
    private var compassAngle = mutableStateOf(0F)
    private var qiblaAngle = mutableStateOf(0F)
    private var accuracyState = mutableStateOf(0)
    private var onPoint = mutableStateOf(false)
    private val dialogShown = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)

        val distanceStr: String
        if (MainActivity.located) {
            location = MainActivity.location!!
            distance = getDistance()
            bearing = calculateBearing()

            setupCompass()
            compass?.start()

            distanceStr = String.format(
                getString(R.string.distance_to_kaaba),
                LangUtils.translateNums(
                    this, distance.toString(), false) + " " + getString(R.string.distance_unit)
            )
        }
        else distanceStr = getString(R.string.location_permission_for_qibla)

        setContent {
            AppTheme {
                UI(distanceStr)
            }
        }
    }

    private fun setupCompass() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Checking features needed for Qibla
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
            && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
            && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
            && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS))
            compass = Compass(this, object : Compass.CompassListener {
                override fun onNewAzimuth(azimuth: Float) {
                    adjust(azimuth)
                    adjustNorthDial(azimuth)
                }

                override fun calibration(accuracy: Int) {
                    accuracyState.value = accuracy
                }
            })
//        else binding.distanceTv.text = getString(R.string.feature_not_supported)
    }

    private fun adjust(azimuth: Float) {
        val target = bearing - currentAzimuth
        currentAzimuth = azimuth

        qiblaAngle.value = target
        onPoint.value = target > -2 && target < 2
    }

    fun adjustNorthDial(azimuth: Float) {
        currentAzimuth = azimuth

        compassAngle.value = -azimuth
    }

    private fun getDistance(): Double {
        val earthRadius = 6371.0
        val dLon = Math.toRadians(abs(location.latitude - kaabaLat))
        val dLat = Math.toRadians(abs(location.longitude - kaabaLng))
        val a = sin(dLat / 2) * sin(dLat / 2) + (cos(Math.toRadians(location.latitude)) *
                cos(Math.toRadians(kaabaLat)) * sin(dLon / 2) * sin(dLon / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        distance = earthRadius * c
        distance = (distance * 10).toInt() / 10.0
        return distance
    }

    private fun calculateBearing(): Float {
        val myLatRad = Math.toRadians(location.latitude)
        val lngDiff = Math.toRadians(kaabaLng - location.longitude)
        val y = sin(lngDiff) * cos(kaabaLatInRad)
        val x = cos(myLatRad) * sin(kaabaLatInRad) -
                (sin(myLatRad) * cos(kaabaLatInRad) * cos(lngDiff))
        return ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()
    }

    @Composable
    private fun UI(distanceStr: String) {
        MyScaffold(stringResource(id = R.string.qibla)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "",
                    tint = Color.Green,
                    modifier = Modifier
                        .alpha(if (onPoint.value) 1F else 0F)
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
                            .rotate(compassAngle.value)
                            .padding(horizontal = 10.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.qibla_pointer),
                        contentDescription = "",
                        modifier = Modifier
                            .rotate(qiblaAngle.value)
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
                        text = when(accuracyState.value) {
                            2 -> { stringResource(id = R.string.medium_accuracy_text) }
                            0, 1 -> { stringResource(id = R.string.low_accuracy_text) }
                            else -> { stringResource(id = R.string.high_accuracy_text) }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    when (accuracyState.value) {
                        0, 1 -> {
                            MyIconBtn(
                                iconId = R.drawable.ic_warning,
                                description = stringResource(
                                    id = R.string.accuracy_indicator_description
                                ),
                                tint = Color(0xFFE2574C)
                            ) {
                                dialogShown.value = true
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (accuracyState.value == 2) Color(0xFF9FAA17)
                                        else Color(0xFF1C8818)
                                    )
                            )
                        }
                    }
                }

                MyText(
                    text = distanceStr,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                if (dialogShown.value) CalibrationDialog()
            }
        }
    }

    @Composable
    private fun CalibrationDialog() {
        MyDialog(dialogShown) {
            Column(
                Modifier.padding(vertical = 20.dp, horizontal = 30.dp)
            ) {
                val imageLoader = ImageLoader.Builder(this@QiblaActivity)
                    .components {
                        if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                        else add(GifDecoder.Factory())
                    }.build()

                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(this@QiblaActivity)
                            .data(data = R.drawable.compass_calibration)
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

}