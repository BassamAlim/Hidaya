package bassamalim.hidaya.helpers

import android.content.Context
import bassamalim.hidaya.other.Utils
import java.util.*
import kotlin.math.*

class PrayTimes(private val context: Context) {

    enum class TF {  // Time Format
        H24,   // 24-hour format
        H12,  // 12-hour format
        H12NS,  // 12-hour format with no suffix
        Floating  // floating point number
    }
    enum class CM {  // Calculation Method
        MECCA,  // Umm al-Qura, Mecca
        MWL,  // Muslim World League
        ISNA,  // Islamic Society of North America
        JAFARI,  // Ithna Ashari
        KARACHI,  // University of Islamic Sciences, Karachi
        EGYPT,  // Egyptian General Authority of Survey
        TAHRAN,  // Institute of Geophysics, University of Tehran
        CUSTOM  // Custom Setting
    }
    enum class JM { SHAFII /*(standard)*/, HANAFI }  // Juristic Methods
    enum class AM {  // Adjusting Methods for Higher Latitudes
        NONE,  // No adjustment
        MIDNIGHT,  // Middle of night
        ONE_SEVENTH,  // 1/7th of night
        ANGLE_BASED  // Angle/60th of night
    }

    // ---------------------- Global Variables --------------------
    private var calcMethod = CM.MECCA // calculation method
    private var timeFormat = TF.H12 // time format
    private var asrJuristic = JM.SHAFII // Juristic method for Asr
    private var adjustHighLats = AM.NONE // adjusting method for higher latitudes
    private var dhuhrMinutes = 0 // minutes after midday for Dhuhr
    private var lat = 0.0 // latitude
    private var lng = 0.0 // longitude
    private var timeZone = 0.0 // time-zone
    private var jDate = 0.0 // Julian date
    // --------------------- Technical Settings --------------------
    private var numIterations = 1 // number of iterations needed to compute times
    // ------------------- Calc Method Parameters --------------------
    private val methodParams = hashMapOf(
        CM.MECCA to doubleArrayOf(18.5, 1.0, 0.0, 1.0, 90.0),
        CM.MWL to doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0),
        CM.ISNA to doubleArrayOf(15.0, 1.0, 0.0, 0.0, 15.0),
        CM.JAFARI to doubleArrayOf(16.0, 0.0, 4.0, 0.0, 14.0),
        CM.KARACHI to doubleArrayOf(18.0, 1.0, 0.0, 0.0, 18.0),
        CM.EGYPT to doubleArrayOf(19.5, 1.0, 0.0, 0.0, 17.5),
        CM.TAHRAN to doubleArrayOf(17.7, 0.0, 4.5, 0.0, 14.0),
        CM.CUSTOM to doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0)
    )
    private val offsets = intArrayOf(0, 0, 0, 0, 0, 0, 0)
    private val invalidTime = "-----" // The string used for invalid times

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

    // radian to degree
    private fun radiansToDegrees(alpha: Double): Double {
        return alpha * 180.0 / Math.PI
    }

    // degree to radian
    private fun degreesToRadians(alpha: Double): Double {
        return alpha * Math.PI / 180.0
    }

    // degree sin
    private fun dSin(d: Double): Double {
        return sin(degreesToRadians(d))
    }

    // degree cos
    private fun dCos(d: Double): Double {
        return cos(degreesToRadians(d))
    }

    // degree tan
    private fun dTan(d: Double): Double {
        return tan(degreesToRadians(d))
    }

    // degree arcSin
    private fun dArcSin(x: Double): Double {
        return radiansToDegrees(asin(x))
    }

    // degree arcCos
    private fun dArcCos(x: Double): Double {
        return radiansToDegrees(acos(x))
    }

    // degree arcTan2
    private fun dArcTan2(y: Double, x: Double): Double {
        return radiansToDegrees(atan2(y, x))
    }

    // degree arcCot
    private fun dArcCot(x: Double): Double {
        return radiansToDegrees(atan2(1.0, x))
    }

    // ---------------------- Time-Zone Functions -----------------------
    // compute local time-zone for a specific date
    private fun getDefaultTimeZone(): Double {
        return TimeZone.getDefault().rawOffset / 1000.0 / 3600
    }

    // detect daylight saving in a given date
    private fun detectDaylightSaving(): Double {
        return TimeZone.getDefault().dstSavings.toDouble()
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

    // convert a calendar date to julian date (second method)
    private fun calcJD(year: Int, month: Int, day: Int): Double {
        val j1970 = 2440588.0
        val cal = Calendar.getInstance()
        cal.set(year, month-1, day)
        val ms = cal.timeInMillis.toDouble() // # of milliseconds since midnight Jan 1, 1970
        val days = floor(ms / (1000.0 * 60.0 * 60.0 * 24.0))
        return j1970 + days - 0.5
    }

    // ---------------------- Calculation Functions -----------------------
    // References:
    // http://www.ummah.net/astronomy/saltime
    // http://aa.usno.navy.mil/faq/docs/SunApprox.html
    // compute declination angle of sun and equation of time
    private fun sunPosition(jd: Double): DoubleArray {
        val dd = jd - 2451545
        val g = fixAngle(357.529 + 0.98560028 * dd)
        val q = fixAngle(280.459 + 0.98564736 * dd)
        val l = fixAngle(q + 1.915 * dSin(g) + 0.020 * dSin(2 * g))

        // double R = 1.00014 - 0.01671 * [self dcos:g] - 0.00014 * [self dcos:
        // (2*g)];
        val e = 23.439 - 0.00000036 * dd
        val d = dArcSin(dSin(e) * dSin(l))
        var ra = dArcTan2(dCos(e) * dSin(l), dCos(l)) / 15.0
        ra = fixHour(ra)
        val eqT = q / 15.0 - ra
        val sPosition = DoubleArray(2)
        sPosition[0] = d
        sPosition[1] = eqT
        return sPosition
    }

    // compute equation of time
    private fun equationOfTime(jd: Double): Double {
        return sunPosition(jd)[1]
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

    // compute time for a given angle G
    private fun computeTime(G: Double, t: Double): Double {
        val d = sunDeclination(jDate + t)
        val z = computeMidDay(t)
        val beg = -dSin(G) - dSin(d) * dSin(lat)
        val mid = dCos(d) * dCos(lat)
        val v = dArcCos(beg / mid) / 15.0
        return z + if (G > 90) -v else v
    }

    // compute the time of Asr
    // Shafii: step=1, Hanafi: step=2
    private fun computeAsr(step: Double, t: Double): Double {
        val d = sunDeclination(jDate + t)
        val g = -dArcCot(step + dTan(abs(lat - d)))
        return computeTime(g, t)
    }

    // ---------------------- Misc Functions -----------------------
    // compute the difference between two times
    private fun timeDiff(time1: Double, time2: Double): Double {
        return fixHour(time2 - time1)
    }

    // -------------------- Interface Functions --------------------
    // return prayer times for a given date
    private fun getDatePrayerTimes(
        year: Int, month: Int, day: Int, latitude: Double, longitude: Double, tZone: Double
    ): ArrayList<String> {
        lat = latitude
        lng = longitude
        timeZone = tZone
        jDate = julianDate(year, month, day)
        jDate -= longitude / (15.0 * 24.0)

        return computeDayTimes()
    }

    fun getPrayerTimes(
        latitude: Double, longitude: Double,
        tZone: Double = getDefaultTimeZone(), date: Calendar = Calendar.getInstance()
    ): Array<Calendar?> {
        return toCalendar(
            getDatePrayerTimes(
                date[Calendar.YEAR], date[Calendar.MONTH] + 1, date[Calendar.DATE],
                latitude, longitude, tZone
            )
        )
    }

    // return prayer times for a given date
    fun getStrPrayerTimes(
        latitude: Double, longitude: Double,
        tZone: Double = getDefaultTimeZone(), date: Calendar = Calendar.getInstance()
    ): ArrayList<String> {

        val result = getDatePrayerTimes(
            date[Calendar.YEAR], date[Calendar.MONTH] + 1, date[Calendar.DATE],
            latitude, longitude, tZone
        )
        result.removeAt(4)

        for (i in result.indices)
            result[i] = Utils.translateNumbers(context, result[i], true)

        return result
    }

    private fun toCalendar(givenTimes: ArrayList<String>): Array<Calendar?> {
        val formattedTimes = arrayOfNulls<Calendar>(givenTimes.size - 1) // subtracted one
        // removing sunset time which is the same as maghrib and pushing others
        givenTimes.removeAt(4)
        for (i in givenTimes.indices) {
            val m = givenTimes[i][6]
            var hour = givenTimes[i].substring(0, 2).toInt()
            if (m == 'p' && hour != 12) hour += 12

            formattedTimes[i] = Calendar.getInstance()
            formattedTimes[i]!!.set(Calendar.HOUR_OF_DAY, hour)
            formattedTimes[i]!!.set(Calendar.MINUTE, givenTimes[i].substring(3, 5).toInt())
            formattedTimes[i]!!.set(Calendar.SECOND, 0)
            formattedTimes[i]!!.set(Calendar.MILLISECOND, 0)
        }
        return formattedTimes
    }

    fun getTomorrowFajr(
        date: Calendar, latitude: Double, longitude: Double, tZone: Double
    ): Calendar {
        val year = date[Calendar.YEAR]
        val month = date[Calendar.MONTH]
        val day = date[Calendar.DATE] + 1

        val str = getDatePrayerTimes(year, month + 1, day, latitude, longitude, tZone)[0]
        var hour = str.substring(0, 2).toInt()
        if (str[6] == 'p') hour += 12

        val calendar = Calendar.getInstance()
        calendar[Calendar.DATE] = day
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = str.substring(3, 5).toInt()
        calendar[Calendar.SECOND] = 0
        return calendar
    }

    // set custom values for calculation parameters
    private fun setCustomParams(params: DoubleArray) {
        for (i in 0..4) {
            if (params[i].toInt() == -1) {
                params[i] = methodParams[calcMethod]?.get(i)!!.toDouble()
                methodParams[CM.CUSTOM] = params
            }
            else methodParams[CM.CUSTOM]?.set(i, params[i])
        }
        calcMethod = CM.CUSTOM
    }

    // set the angle for calculating Fajr
    fun setFajrAngle(angle: Double) {
        val params = doubleArrayOf(angle, -1.0, -1.0, -1.0, -1.0)
        setCustomParams(params)
    }

    // set the angle for calculating Maghrib
    fun setMaghribAngle(angle: Double) {
        val params = doubleArrayOf(-1.0, 0.0, angle, -1.0, -1.0)
        setCustomParams(params)
    }

    // set the angle for calculating Isha
    fun setIshaAngle(angle: Double) {
        val params = doubleArrayOf(-1.0, -1.0, -1.0, 0.0, angle)
        setCustomParams(params)
    }

    // set the minutes after Sunset for calculating Maghrib
    fun setMaghribMinutes(minutes: Double) {
        val params = doubleArrayOf(-1.0, 1.0, minutes, -1.0, -1.0)
        setCustomParams(params)
    }

    // set the minutes after Maghrib for calculating Isha
    fun setIshaMinutes(minutes: Double) {
        val params = doubleArrayOf(-1.0, -1.0, -1.0, 1.0, minutes)
        setCustomParams(params)
    }

    // convert double hours to 24h format
    private fun floatToTime24(gTime: Double): String {
        var time = gTime
        val result: String
        if (java.lang.Double.isNaN(time)) return invalidTime

        time = fixHour(time + 0.5 / 60.0) // add 0.5 minutes to round
        val hours = floor(time).toInt()
        val minutes = floor((time - hours) * 60.0)

        result =
            if (hours in 0..9 && minutes >= 0 && minutes <= 9)
                "0" + hours + ":0" + minutes.roundToInt()
            else if (hours in 0..9)
                "0" + hours + ":" + minutes.roundToInt()
            else if (minutes in 0.0..9.0)
                hours.toString() + ":0" + minutes.roundToInt()
            else
                hours.toString() + ":" + minutes.roundToInt()

        return result
    }

    // convert double hours to 12h format
    private fun floatToTime12(gTime: Double, noSuffix: Boolean): String {
        var time = gTime
        if (java.lang.Double.isNaN(time)) return invalidTime

        time = fixHour(time + 0.5 / 60) // add 0.5 minutes to round
        var hours = floor(time).toInt()
        val minutes = floor((time - hours) * 60)
        val result: String
        val suffix: String =
            if (hours >= 12) "pm"
            else "am"
        hours = (hours + 12 - 1) % 12 + 1

        result =
            if (!noSuffix) {
                if (hours in 0..9 && minutes >= 0 && minutes <= 9)
                    ("0" + hours + ":0" + minutes.roundToInt() + " " + suffix)
                else if (hours in 0..9)
                    "0" + hours + ":" + minutes.roundToInt() + " " + suffix
                else if (minutes in 0.0..9.0)
                    hours.toString() + ":0" + minutes.roundToInt() + " " + suffix
                else
                    hours.toString() + ":" + minutes.roundToInt() + " " + suffix
            }
            else {
                if (hours in 0..9 && minutes >= 0 && minutes <= 9)
                    "0" + hours + ":0" + minutes.roundToInt()
                else if (hours in 0..9)
                    "0" + hours + ":" + minutes.roundToInt()
                else if (minutes in 0.0..9.0)
                    hours.toString() + ":0" + minutes.roundToInt()
                else
                    hours.toString() + ":" + minutes.roundToInt()
            }
        return result
    }

    // convert double hours to 12h format with no suffix
    fun floatToTime12NS(time: Double): String {
        return floatToTime12(time, true)
    }

    // ---------------------- Compute Prayer Times -----------------------
    // compute prayer times at given julian date
    private fun computeTimes(times: DoubleArray): DoubleArray {
        val t = dayPortion(times)
        val fajr = computeTime(180 - (methodParams[calcMethod]?.get(0)!!), t[0])
        val sunrise = computeTime(180 - 0.833, t[1])
        val dhuhr = computeMidDay(t[2])
        val asr = computeAsr((1 + asrJuristic.ordinal).toDouble(), t[3])
        val sunset = computeTime(0.833, t[4])
        val maghrib = computeTime(methodParams[calcMethod]?.get(2)!!, t[5])
        val isha = computeTime(methodParams[calcMethod]?.get(4)!!, t[6])
        return doubleArrayOf(fajr, sunrise, dhuhr, asr, sunset, maghrib, isha)
    }

    // compute prayer times at given julian date
    private fun computeDayTimes(): ArrayList<String> {
        var times = doubleArrayOf(5.0, 6.0, 12.0, 13.0, 18.0, 18.0, 18.0) // default times
        for (i in 1..numIterations) times = computeTimes(times)
        adjustTimes(times)
        tuneTimes(times)
        return adjustTimesFormat(times)
    }

    // adjust times in a prayer time array
    private fun adjustTimes(times: DoubleArray): DoubleArray {
        for (i in times.indices) times[i] += timeZone - lng / 15

        times[2] += dhuhrMinutes / 60.0 // Dhuhr
        if (methodParams[calcMethod]?.get(1)?.toInt() == 1) // Maghrib
            times[5] = times[4] + (methodParams[calcMethod]?.get(2)!!) / 60
        if (methodParams[calcMethod]?.get(3)?.toInt() == 1) // Isha
            times[6] = times[5] + (methodParams[calcMethod]?.get(4)!!) / 60
        if (adjustHighLats != AM.NONE)
            adjustHighLatTimes(times)

        return times
    }

    // convert times array to given time format
    private fun adjustTimesFormat(times: DoubleArray): ArrayList<String> {
        val result = ArrayList<String>()
        for (time in times) {
            when(timeFormat) {
                TF.H24 -> result.add(floatToTime24(time))
                TF.H12 -> result.add(floatToTime12(time, false))
                TF.H12NS -> result.add(floatToTime12(time, true))
                TF.Floating -> result.add(time.toString())
            }
        }
        return result
    }

    // adjust Fajr, Isha and Maghrib for locations in higher latitudes
    private fun adjustHighLatTimes(times: DoubleArray): DoubleArray {
        val nightTime = timeDiff(times[4], times[1]) // sunset to sunrise

        // Adjust Fajr
        val fajrDiff = nightPortion(methodParams[calcMethod]?.get(0)!!) * nightTime
        if (java.lang.Double.isNaN(times[0]) || timeDiff(times[0], times[1]) > fajrDiff)
            times[0] = times[1] - fajrDiff

        // Adjust Isha
        val ishaAngle: Double =
            if (methodParams[calcMethod]?.get(3)?.toInt() == 0)
                methodParams[calcMethod]?.get(4)!!.toDouble()
            else 18.0

        val ishaDiff = nightPortion(ishaAngle) * nightTime
        if (java.lang.Double.isNaN(times[6]) || timeDiff(times[4], times[6]) > ishaDiff)
            times[6] = times[4] + ishaDiff

        // Adjust Maghrib
        val maghribAngle: Double =
            if (methodParams[calcMethod]?.get(1)?.toInt() == 0)
                methodParams[calcMethod]?.get(2)!!.toDouble()
            else 4.0

        val maghribDiff = nightPortion(maghribAngle) * nightTime
        if (java.lang.Double.isNaN(times[5]) || timeDiff(times[4], times[5]) > maghribDiff)
            times[5] = times[4] + maghribDiff

        return times
    }

    // the night portion used for adjusting times in higher latitudes
    private fun nightPortion(angle: Double): Double {
        return when (adjustHighLats) {
            AM.NONE -> 0.0
            AM.MIDNIGHT -> 0.5
            AM.ONE_SEVENTH -> 0.14286
            AM.ANGLE_BASED -> angle / 60.0
        }
    }

    // convert hours to day portions
    private fun dayPortion(times: DoubleArray): DoubleArray {
        for (i in 0..6) times[i] = times[i] / 24
        return times
    }

    // Tune timings for adjustments (Set time offsets)
    fun tune(offsetTimes: IntArray) {
        System.arraycopy(offsetTimes, 0, offsets, 0, offsetTimes.size)
    }

    private fun tuneTimes(times: DoubleArray): DoubleArray {
        for (i in times.indices) times[i] = times[i] + offsets[i] / 60.0
        return times
    }

}