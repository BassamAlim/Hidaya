package bassamalim.hidaya.helpers

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import bassamalim.hidaya.R
import bassamalim.hidaya.other.Global

class Compass(gContext: Context) : SensorEventListener {

    interface CompassListener {
        fun onNewAzimuth(azimuth: Float)
        fun calibration(accuracy: Int)
    }

    private var listener: CompassListener? = null
    private val sensorManager: SensorManager
    private val mSensor: Sensor
    private val aSensor: Sensor
    private val aData = FloatArray(3)
    private val mData = FloatArray(3)
    private val myR = FloatArray(9)
    private val myI = FloatArray(9)

    /**
     * Register the listeners for the accelerometer and magnetometer sensors
     *
     * @param context The context of the calling class.
     */
    fun start(context: Context) {
        sensorManager.registerListener(
            this, aSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            this, mSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        val manager = context.packageManager
        val haveAS = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
        val haveCS = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)
        if (!haveAS || !haveCS) {
            sensorManager.unregisterListener(this, aSensor)
            sensorManager.unregisterListener(this, mSensor)
            Toast.makeText(
                context, context.getString(R.string.feature_not_supported),
                Toast.LENGTH_SHORT
            ).show()
            Log.e(Global.TAG, "Device don't have enough sensors")
        }
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
                var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                azimuth = (azimuth + 360) % 360

                if (listener != null) listener!!.onNewAzimuth(azimuth)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        listener!!.calibration(accuracy)
    }

    fun setListener(l: CompassListener?) {
        listener = l
    }

    init {
        sensorManager = gContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }
}