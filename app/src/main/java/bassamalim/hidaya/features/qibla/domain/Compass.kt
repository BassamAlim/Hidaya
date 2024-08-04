package bassamalim.hidaya.features.qibla.domain

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class Compass(
    context: Context,
    private var listener: CompassListener
) : SensorEventListener {

    private val sensorManager: SensorManager
    private val mSensor: Sensor?
    private val aSensor: Sensor?
    private val aData = FloatArray(3)
    private val mData = FloatArray(3)
    private val myR = FloatArray(9)
    private val myI = FloatArray(9)

    interface CompassListener {
        fun onNewAzimuth(azimuth: Float)
        fun calibration(accuracy: Int)
    }

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    /**
     * Register the listeners for the accelerometer and magnetometer sensors
     */
    fun start() {
        sensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    // It's a way to make sure that the code is executed in a synchronized way.
    override fun onSensorChanged(event: SensorEvent) {
        synchronized(this) {
            val alpha = 0.97f
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                aData[0] = alpha * aData[0] + (1 - alpha) * event.values[0]
                aData[1] = alpha * aData[1] + (1 - alpha) * event.values[1]
                aData[2] = alpha * aData[2] + (1 - alpha) * event.values[2]
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                mData[0] = alpha * mData[0] + (1 - alpha) * event.values[0]
                mData[1] = alpha * mData[1] + (1 - alpha) * event.values[1]
                mData[2] = alpha * mData[2] + (1 - alpha) * event.values[2]
            }

            val success = SensorManager.getRotationMatrix(myR, myI, aData, mData)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(myR, orientation)
                var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()    // orientation
                azimuth = (azimuth + 360) % 360

                listener.onNewAzimuth(azimuth)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        listener.calibration(accuracy)
    }

}