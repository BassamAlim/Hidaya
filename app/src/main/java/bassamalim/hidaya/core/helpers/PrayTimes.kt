package bassamalim.hidaya.core.helpers

import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.models.Coordinates
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

class PrayTimes(private val settings: PrayerTimeCalculatorSettings) {

    private var asrJuristic =
        if (settings.juristicMethod == PrayerTimeJuristicMethod.HANAFI) 1
        else 0

    private var dhuhrMinutes = 0 // minutes after midday for Dhuhr
    private var latitude = 0.0 // latitude
    private var longitude = 0.0 // longitude
    private var timeZone = 0.0 // time-zone UTC Offset
    private var jDate = 0.0 // Julian date
    private var numIterations = 1 // number of iterations needed to compute times

    private val methodParams = hashMapOf(
        PrayerTimeCalculationMethod.MECCA to doubleArrayOf(18.5, 1.0, 0.0, 1.0, 90.0),
        PrayerTimeCalculationMethod.MWL to doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0),
        PrayerTimeCalculationMethod.ISNA to doubleArrayOf(15.0, 1.0, 0.0, 0.0, 15.0),
        PrayerTimeCalculationMethod.KARACHI to doubleArrayOf(18.0, 1.0, 0.0, 0.0, 18.0),
        PrayerTimeCalculationMethod.EGYPT to doubleArrayOf(19.5, 1.0, 0.0, 0.0, 17.5),
        PrayerTimeCalculationMethod.TAHRAN to doubleArrayOf(17.7, 0.0, 4.5, 0.0, 14.0)
    )

    // -------------------- Interface Functions --------------------
    // returns prayer times in Calendar object
    fun getPrayerTimes(coordinates: Coordinates, calendar: Calendar): Array<Calendar?> {
        setValues(
            coordinates.latitude,
            coordinates.longitude,
            calendar[Calendar.ZONE_OFFSET].toDouble() / 3600000.0,
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH] + 1,
            calendar[Calendar.DATE]
        )

        val times = floatToTime24(computeDayTimes())

        times.removeAt(4)  // removing sunset time

        val cals = arrayOfNulls<Calendar>(times.size)
        for (i in times.indices) {
            val nums = times[i].split(":")

            val cal = Calendar.getInstance()
            cal[Calendar.HOUR_OF_DAY] = nums[0].toInt()
            cal[Calendar.MINUTE] = nums[1].toInt()
            cal[Calendar.SECOND] = 0
            cal[Calendar.MILLISECOND] = 0
            cals[i] = cal
        }

        // add offsets

        return cals
    }

    private fun setValues(
        lat: Double, lng: Double, tZone: Double, year: Int, month: Int, day: Int
    ) {
        latitude = lat
        longitude = lng
        timeZone = tZone
        jDate = julianDate(year, month, day)
        jDate -= lng / (15.0 * 24.0)
    }

    // ---------------------- Time-Zone Functions -----------------------
    // compute local time-zone for a specific date
    private fun getDefaultTimeZone(): Double {
        return TimeZone.getDefault().rawOffset / 3600000.0
    }

    // ---------------------- Julian Date Functions -----------------------
    // calculate julian date from a calendar date
    private fun julianDate(gYear: Int, gMonth: Int, gDay: Int): Double {
        var year = gYear
        var month = gMonth
        if (month <= 2) {
            year -= 1
            month += 12
        }

        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return (
                floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + gDay + b
                ) - 1524.5
    }

    // compute prayer times at given julian date
    private fun computeDayTimes(): DoubleArray {
        var times = doubleArrayOf(5.0, 6.0, 12.0, 13.0, 18.0, 18.0, 18.0)  // default times
        for (i in 1..numIterations) times = computeTimes(times)
        adjustTimes(times)
        return times
    }

    // compute prayer times at given julian date
    private fun computeTimes(times: DoubleArray): DoubleArray {
        val t = dayPortion(times)
        val fajr = computeTime(180 - (methodParams[settings.calculationMethod]!![0]), t[0])
        val sunrise = computeTime(180 - 0.833, t[1])
        val dhuhr = computeMidDay(t[2])
        val asr = computeAsr((1 + asrJuristic).toDouble(), t[3])
        val sunset = computeTime(0.833, t[4])
        val maghrib = computeTime(methodParams[settings.calculationMethod]!![2], t[5])
        val isha = computeTime(methodParams[settings.calculationMethod]!![4], t[6])
        return doubleArrayOf(fajr, sunrise, dhuhr, asr, sunset, maghrib, isha)
    }

    // compute the time of Asr
    // Shafii: step=1, Hanafi: step=2
    private fun computeAsr(step: Double, t: Double): Double {
        val d = sunDeclination(jDate + t)
        val g = -degreeArcCot(step + degreeTan(abs(latitude - d)))
        return computeTime(g, t)
    }

    // compute time for a given angle G
    // Error here in some cases such as poland (v = NaN)
    private fun computeTime(G: Double, t: Double): Double {
        val d = sunDeclination(jDate + t)
        val z = computeMidDay(t)
        val beg = -degreeSin(G) - degreeSin(d) * degreeSin(latitude)
        val mid = degreeCos(d) * degreeCos(latitude)
        val v = degreeArcCos(beg / mid) / 15.0
        return z + if (G > 90) -v else v
    }

    // compute declination angle of sun
    private fun sunDeclination(jd: Double): Double {
        return sunPosition(jd)[0]
    }

    // compute mid-day (Dhuhr, Zawal) time
    private fun computeMidDay(gT: Double): Double {
        val t = equationOfTime(jDate + gT)
        return fixHour(12 - t)
    }

    // compute equation of time
    private fun equationOfTime(jd: Double): Double {
        return sunPosition(jd)[1]
    }

    // compute declination angle of sun and equation of time
    private fun sunPosition(jd: Double): DoubleArray {
        val dd = jd - 2451545
        val g = fixAngle(357.529 + 0.98560028 * dd)
        val q = fixAngle(280.459 + 0.98564736 * dd)
        val l = fixAngle(q + 1.915 * degreeSin(g) + 0.020 * degreeSin(2 * g))

        // double R = 1.00014 - 0.01671 * [self dCos:g] - 0.00014 * [self dCos:
        // (2*g)];
        val e = 23.439 - 0.00000036 * dd
        val d = degreeArcSin(degreeSin(e) * degreeSin(l))
        var ra = degreeArcTan2(degreeCos(e) * degreeSin(l), degreeCos(l)) / 15.0
        ra = fixHour(ra)
        val eqT = q / 15.0 - ra
        val sPosition = DoubleArray(2)
        sPosition[0] = d
        sPosition[1] = eqT
        return sPosition
    }

    // adjust times in a prayer time array
    private fun adjustTimes(times: DoubleArray): DoubleArray {
        for (i in times.indices) times[i] += timeZone - longitude / 15

        times[2] += dhuhrMinutes / 60.0 // Dhuhr
        if (methodParams[settings.calculationMethod]?.get(1)?.toInt() == 1) // Maghrib
            times[5] = times[4] + (methodParams[settings.calculationMethod]?.get(2)!!) / 60
        if (methodParams[settings.calculationMethod]?.get(3)?.toInt() == 1) // Isha
            times[6] = times[5] + (methodParams[settings.calculationMethod]?.get(4)!!) / 60
        if (settings.highLatitudesAdjustmentMethod != HighLatitudesAdjustmentMethod.NONE)
            adjustHighLatTimes(times)

        return times
    }

    // adjust Fajr, Isha and Maghrib for locations in higher latitudes
    private fun adjustHighLatTimes(times: DoubleArray): DoubleArray {
        val nightTime = timeDiff(times[4], times[1]) // sunset to sunrise

        // Adjust Fajr
        val fajrDiff = nightPortion(methodParams[settings.calculationMethod]?.get(0)!!) * nightTime
        if (java.lang.Double.isNaN(times[0]) || timeDiff(times[0], times[1]) > fajrDiff)
            times[0] = times[1] - fajrDiff

        // Adjust Isha
        val ishaAngle =
            if (methodParams[settings.calculationMethod]?.get(3)?.toInt() == 0)
                methodParams[settings.calculationMethod]?.get(4)!!.toDouble()
            else 18.0

        val ishaDiff = nightPortion(ishaAngle) * nightTime
        if (java.lang.Double.isNaN(times[6]) || timeDiff(times[4], times[6]) > ishaDiff)
            times[6] = times[4] + ishaDiff

        // Adjust Maghrib
        val maghribAngle =
            if (methodParams[settings.calculationMethod]?.get(1)?.toInt() == 0)
                methodParams[settings.calculationMethod]?.get(2)!!.toDouble()
            else 4.0

        val maghribDiff = nightPortion(maghribAngle) * nightTime
        if (java.lang.Double.isNaN(times[5]) || timeDiff(times[4], times[5]) > maghribDiff)
            times[5] = times[4] + maghribDiff

        return times
    }

    // compute the difference between two times
    private fun timeDiff(time1: Double, time2: Double): Double {
        return fixHour(time2 - time1)
    }

    // convert hours to day portions
    private fun dayPortion(times: DoubleArray): DoubleArray {
        for (i in 0..6) times[i] = times[i] / 24
        return times
    }

    // the night portion used for adjusting times in higher latitudes
    private fun nightPortion(angle: Double): Double {
        return when (settings.highLatitudesAdjustmentMethod) {
            HighLatitudesAdjustmentMethod.MIDNIGHT -> 0.5
            HighLatitudesAdjustmentMethod.ONE_SEVENTH -> 0.14286
            HighLatitudesAdjustmentMethod.ANGLE_BASED -> angle / 60.0
            else -> 0.0
        }
    }

    // convert double hours to 24h format
    private fun floatToTime24(times: DoubleArray): ArrayList<String> {
        val result = ArrayList<String>()
        for (time in times) {
            val fixed = fixHour(time + 0.5 / 60.0) // add 0.5 minutes to round
            val hours = floor(fixed).toInt()
            val minutes = floor((fixed - hours) * 60.0)

            result.add(
                if (hours in 0..9 && minutes >= 0 && minutes <= 9)
                    "0" + hours + ":0" + minutes.roundToInt()
                else if (hours in 0..9)
                    "0" + hours + ":" + minutes.roundToInt()
                else if (minutes in 0.0..9.0)
                    hours.toString() + ":0" + minutes.roundToInt()
                else
                    hours.toString() + ":" + minutes.roundToInt()
            )
        }
        return result
    }

    // ---------------------- Trigonometric Functions -----------------------

    // range reduce angle in degrees.
    private fun fixAngle(gA: Double): Double {
        var a = gA
        a -= 360 * floor(a / 360.0)
        a = if (a < 0) a + 360 else a
        return a
    }

    // range reduce hours to 0..23
    private fun fixHour(gA: Double): Double {
        var a = gA
        a -= 24.0 * floor(a / 24.0)
        a = if (a < 0) a + 24 else a
        return a
    }

    private fun degreeSin(d: Double) = sin(degreesToRadians(d))

    private fun degreeCos(d: Double) = cos(degreesToRadians(d))

    private fun degreeTan(d: Double) = tan(degreesToRadians(d))

    private fun degreeArcSin(x: Double) = radiansToDegrees(asin(x))

    private fun degreeArcCos(x: Double) = radiansToDegrees(acos(x))

    private fun degreeArcTan2(y: Double, x: Double) = radiansToDegrees(atan2(y, x))

    private fun degreeArcCot(x: Double) = radiansToDegrees(atan2(1.0, x))

    private fun radiansToDegrees(alpha: Double) = alpha * 180.0 / Math.PI

    private fun degreesToRadians(alpha: Double) = alpha * Math.PI / 180.0

}