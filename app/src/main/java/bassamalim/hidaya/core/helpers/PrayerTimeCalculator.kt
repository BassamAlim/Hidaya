package bassamalim.hidaya.core.helpers

import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.models.Coordinates
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import java.util.Calendar
import java.util.SortedMap
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

class PrayerTimeCalculator(
    private val settings: PrayerTimeCalculatorSettings,
    private val timeOffsets: Map<PID, Int>
) {

    private var asrJuristic =
        if (settings.juristicMethod == PrayerTimeJuristicMethod.HANAFI) 1
        else 0
    private var dhuhrMinutes = 0 // minutes after midday for Dhuhr
    private val offsets = intArrayOf(0, 0, 0, 0, 0, 0, 0)
    private var numIterations = 1 // number of iterations needed to compute times

    private val methodParams = hashMapOf(
        PrayerTimeCalculationMethod.MECCA to doubleArrayOf(18.5, 1.0, 0.0, 1.0, 90.0),
        PrayerTimeCalculationMethod.MWL to doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0),
        PrayerTimeCalculationMethod.ISNA to doubleArrayOf(15.0, 1.0, 0.0, 0.0, 15.0),
        PrayerTimeCalculationMethod.KARACHI to doubleArrayOf(18.0, 1.0, 0.0, 0.0, 18.0),
        PrayerTimeCalculationMethod.EGYPT to doubleArrayOf(19.5, 1.0, 0.0, 0.0, 17.5),
        PrayerTimeCalculationMethod.TAHRAN to doubleArrayOf(17.7, 0.0, 4.5, 0.0, 14.0)
    )

    init {
        setOffsets()
    }

    // Tune timings for adjustments (Set time offsets)
    private fun setOffsets() {
        offsets[0] = timeOffsets[PID.FAJR]!!
        offsets[1] = timeOffsets[PID.SUNRISE]!!
        offsets[2] = timeOffsets[PID.DHUHR]!!
        offsets[3] = timeOffsets[PID.ASR]!!
        // Skipping sunset
        offsets[5] = timeOffsets[PID.MAGHRIB]!!
        offsets[6] = timeOffsets[PID.ISHAA]!!
    }

    // -------------------- Interface Functions --------------------
    // returns prayer times in Calendar object
    fun getPrayerTimes(
        coordinates: Coordinates,
        utcOffset: Double = getDefaultUtcOffset(),
        calendar: Calendar = Calendar.getInstance()
    ): SortedMap<PID, Calendar?> {
        val julianDate = getJulianDate(calendar = calendar, longitude = coordinates.longitude)

        val times = computeDayTimes(
            coordinates = coordinates,
            utcOffset = utcOffset,
            julianDate = julianDate
        )

        return sortedMapOf(
            PID.FAJR to buildCalendar(times[0]),
            PID.SUNRISE to buildCalendar(times[1]),
            PID.DHUHR to buildCalendar(times[2]),
            PID.ASR to buildCalendar(times[3]),
            // skipping sunset time
            PID.MAGHRIB to buildCalendar(times[5]),
            PID.ISHAA to buildCalendar(times[6])
        )
    }

    private fun buildCalendar(time: Double): Calendar {
        val fixed = fixHour(time + 0.5 / 60.0) // add 0.5 minutes to round
        val hours = floor(fixed).toInt()
        val minutes = floor((fixed - hours) * 60.0).roundToInt()

        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = hours
        cal[Calendar.MINUTE] = minutes
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal
    }

    // ---------------------- Time-Zone Functions -----------------------
    // compute local time-zone for a specific date
    private fun getDefaultUtcOffset(): Double {
        return TimeZone.getDefault().rawOffset / 3600000.0
    }

    // ---------------------- Julian Date Functions -----------------------
    // calculate julian date from a calendar date
    private fun getJulianDate(
        calendar: Calendar,
        longitude: Double
    ): Double {
        var year = calendar[Calendar.YEAR]
        var month = calendar[Calendar.MONTH] + 1
        val day = calendar[Calendar.DATE]
        if (month <= 2) {
            year -= 1
            month += 12
        }

        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)
        var jDate = (
                floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + day + b
                ) - 1524.5
        jDate -= longitude / (15.0 * 24.0)
        return jDate
    }

    // compute prayer times at given julian date
    private fun computeDayTimes(
        coordinates: Coordinates,
        utcOffset: Double,
        julianDate: Double
    ): DoubleArray {
        var times = doubleArrayOf(5.0, 6.0, 12.0, 13.0, 18.0, 18.0, 18.0)  // default times
        for (i in 1..numIterations) times = computeTimes(
            times = times,
            latitude = coordinates.latitude,
            julianDate = julianDate
        )
        times = adjustTimes(times = times, utcOffset = utcOffset, longitude = coordinates.longitude)
        times = tuneTimes(times)
        return times
    }

    // compute prayer times at given julian date
    private fun computeTimes(
        times: DoubleArray,
        latitude: Double,
        julianDate: Double
    ): DoubleArray {
        val t = dayPortion(times)
        val fajr = computeTime(
            g = 180 - (methodParams[settings.calculationMethod]!![0]),
            t = t[0],
            latitude = latitude,
            julianDate = julianDate
        )
        val sunrise = computeTime(
            g = 180 - 0.833,
            t = t[1],
            latitude = latitude,
            julianDate = julianDate
        )
        val dhuhr = computeMidDay(gT = t[2], julianDate = julianDate)
        val asr = computeAsr(
            step = (1 + asrJuristic).toDouble(),
            t = t[3],
            latitude = latitude,
            julianDate = julianDate
        )
        val sunset = computeTime(g = 0.833, t = t[4], latitude = latitude, julianDate = julianDate)
        val maghrib = computeTime(
            g = methodParams[settings.calculationMethod]!![2],
            t = t[5],
            latitude = latitude,
            julianDate = julianDate
        )
        val isha = computeTime(
            g = methodParams[settings.calculationMethod]!![4],
            t = t[6],
            latitude = latitude,
            julianDate = julianDate
        )
        return doubleArrayOf(fajr, sunrise, dhuhr, asr, sunset, maghrib, isha)
    }

    // compute the time of Asr
    // Shafii: step=1, Hanafi: step=2
    private fun computeAsr(
        step: Double,
        t: Double,
        latitude: Double,
        julianDate: Double
    ): Double {
        val d = sunDeclination(julianDate + t)
        val g = -degreeArcCot(step + degreeTan(abs(latitude - d)))
        return computeTime(g = g, t = t, latitude = latitude, julianDate = julianDate)
    }

    // compute time for a given angle G
    // Error here in some cases such as poland (v = NaN)
    private fun computeTime(
        g: Double,
        t: Double,
        latitude: Double,
        julianDate: Double
    ): Double {
        val d = sunDeclination(julianDate + t)
        val z = computeMidDay(gT = t, julianDate = julianDate)
        val beg = -degreeSin(g) - degreeSin(d) * degreeSin(latitude)
        val mid = degreeCos(d) * degreeCos(latitude)
        val v = degreeArcCos(beg / mid) / 15.0
        return z + if (g > 90) -v else v
    }

    // compute declination angle of sun
    private fun sunDeclination(jd: Double) =
        sunPosition(jd)[0]

    // compute mid-day (Dhuhr, Zawal) time
    private fun computeMidDay(gT: Double, julianDate: Double): Double {
        val t = equationOfTime(julianDate + gT)
        return fixHour(12 - t)
    }

    // compute equation of time
    private fun equationOfTime(jd: Double) =
        sunPosition(jd)[1]

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
    private fun adjustTimes(
        times: DoubleArray,
        utcOffset: Double,
        longitude: Double
    ): DoubleArray {
        for (i in times.indices) times[i] += utcOffset - longitude / 15

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
    private fun timeDiff(time1: Double, time2: Double) =
        fixHour(time2 - time1)

    // convert hours to day portions
    private fun dayPortion(times: DoubleArray) =
        times.map { time ->
            time / 24.0
        }.toDoubleArray()

    // the night portion used for adjusting times in higher latitudes
    private fun nightPortion(angle: Double): Double {
        return when (settings.highLatitudesAdjustmentMethod) {
            HighLatitudesAdjustmentMethod.MIDNIGHT -> 0.5
            HighLatitudesAdjustmentMethod.ONE_SEVENTH -> 0.14286
            HighLatitudesAdjustmentMethod.ANGLE_BASED -> angle / 60.0
            else -> 0.0
        }
    }

    private fun tuneTimes(times: DoubleArray): DoubleArray =
        times.mapIndexed { i, time ->
            time + offsets[i] / 60.0
        }.toDoubleArray()

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