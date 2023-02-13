package bassamalim.hidaya.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.helpers.Compass
import bassamalim.hidaya.repository.QiblaRepo
import bassamalim.hidaya.state.QiblaState
import bassamalim.hidaya.utils.LangUtils.translateNums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.*

@HiltViewModel
class QiblaVM @Inject constructor(
    private val app: Application,
    private val repository: QiblaRepo
): AndroidViewModel(app) {

    private val kaabaLat = 21.4224779
    private val kaabaLng = 39.8251832
    private val kaabaLatInRad = Math.toRadians(kaabaLat)
    private var compass: Compass? = null
    private val location = repository.getLocation()
    private var currentAzimuth = 0F
    private var bearing = 0F

    private val _uiState = MutableStateFlow(QiblaState())
    val uiState = _uiState.asStateFlow()

    init {
        if (location != null) {
            _uiState.update { it.copy(
                distanceToKaaba = translateNums(
                    repository.numeralsLanguage(),
                    getDistance().toString()
                )
            )}

            bearing = calculateBearing()

            setupCompass()
        }
        else {
            _uiState.update { it.copy(
                error = true,
                errorMassageResId = R.string.location_permission_for_qibla
            )}
        }
    }

    fun onStart() {
        if (location != null) {
            compass!!.start()
        }
    }

    fun onStop() {
        compass?.stop()
    }

    private fun setupCompass() {
        val sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Checking features needed for Qibla
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
            && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
            && app.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
            && app.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS))
            compass = Compass(app, object : Compass.CompassListener {
                override fun onNewAzimuth(azimuth: Float) {
                    adjust(azimuth)
                    adjustNorthDial(azimuth)
                }

                override fun calibration(accuracy: Int) {
                    _uiState.update { it.copy(
                        accuracy = accuracy
                    )}
                }
            })
        else {
            _uiState.update { it.copy(
                error = true,
                errorMassageResId = R.string.feature_not_supported
            )}
        }
    }

    private fun adjust(azimuth: Float) {
        val target = bearing - currentAzimuth
        currentAzimuth = azimuth

        _uiState.update { it.copy(
            qiblaAngle = target,
            isOnPoint = target > -2 && target < 2
        )}
    }

    fun adjustNorthDial(azimuth: Float) {
        currentAzimuth = azimuth

        _uiState.update { it.copy(
            compassAngle = -azimuth
        )}
    }

    private fun getDistance(): Double {
        val earthRadius = 6371.0
        val dLon = Math.toRadians(abs(location!!.latitude - kaabaLat))
        val dLat = Math.toRadians(abs(location.longitude - kaabaLng))
        val a = sin(dLat / 2) * sin(dLat / 2) + (cos(Math.toRadians(location.latitude)) *
                cos(Math.toRadians(kaabaLat)) * sin(dLon / 2) * sin(dLon / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        var distance = earthRadius * c
        distance = (distance * 10).toInt() / 10.0
        return distance
    }

    private fun calculateBearing(): Float {
        val myLatRad = Math.toRadians(location!!.latitude)
        val lngDiff = Math.toRadians(kaabaLng - location.longitude)
        val y = sin(lngDiff) * cos(kaabaLatInRad)
        val x = cos(myLatRad) * sin(kaabaLatInRad) -
                (sin(myLatRad) * cos(kaabaLatInRad) * cos(lngDiff))
        return ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()
    }

    fun onAccuracyIndicatorClick() {
        _uiState.update { it.copy(
            calibrationDialogShown = true
        )}
    }

    fun onCalibrationDialogDismiss() {
        _uiState.update { it.copy(
            calibrationDialogShown = false
        )}
    }

}