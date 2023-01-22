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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

}