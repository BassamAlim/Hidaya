package bassamalim.hidaya.models

import android.location.Location
import android.os.Build
import java.io.Serializable

data class MyLocation(val loc: Location) : Serializable {
    val accuracy: Float = loc.accuracy
    val time: Long = loc.time
    val altitude: Double = loc.altitude
    val bearing: Float = loc.bearing
    var bearingAccuracyDegrees = 0f
    val elapsedRealtimeNanos: Long = loc.elapsedRealtimeNanos
    private var elapsedRealtimeUncertaintyNanos = 0.0
    val latitude: Double = loc.latitude
    val longitude: Double = loc.longitude
    val provider: String = loc.provider
    val speed: Float = loc.speed
    var speedAccuracyMetersPerSecond = 0f
    var verticalAccuracyMeters = 0f

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bearingAccuracyDegrees = loc.bearingAccuracyDegrees
            speedAccuracyMetersPerSecond = loc.speedAccuracyMetersPerSecond
            verticalAccuracyMeters = loc.verticalAccuracyMeters
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) elapsedRealtimeUncertaintyNanos =
                loc.elapsedRealtimeUncertaintyNanos
        }
    }

    companion object {    // companion object is like a container for static methods
        fun toLocation(myLoc: MyLocation): Location {
            val loc = Location("provider")
            loc.accuracy = myLoc.accuracy
            loc.time = myLoc.time
            loc.altitude = myLoc.altitude
            loc.bearing = myLoc.bearing
            loc.elapsedRealtimeNanos = myLoc.elapsedRealtimeNanos
            loc.latitude = myLoc.latitude
            loc.longitude = myLoc.longitude
            loc.provider = myLoc.provider
            loc.speed = myLoc.speed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                loc.bearingAccuracyDegrees = myLoc.bearingAccuracyDegrees
                loc.speedAccuracyMetersPerSecond = myLoc.speedAccuracyMetersPerSecond
                loc.verticalAccuracyMeters = myLoc.verticalAccuracyMeters
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    loc.elapsedRealtimeUncertaintyNanos = loc.elapsedRealtimeUncertaintyNanos
            }
            return loc
        }
    }
}