package bassamalim.hidaya.helpers

import android.content.Context
import bassamalim.hidaya.other.Utils
import java.util.*
import kotlin.math.*

class PrayTimes(private val context: Context) {
    // ---------------------- Global Variables --------------------
    private var calcMethod = 4 // calculation method
    private var asrJuristic = 0 // Juristic method for Asr
    private var dhuhrMinutes = 0 // minutes after midday for Dhuhr
    private var adjustHighLats = 0 // adjusting method for higher latitudes
    private var timeFormat = 1 // time format
    private var lat = 0.0 // latitude
    private var lng = 0.0 // longitude
    private var timeZone = 0.0 // time-zone
    private var jDate = 0.0 // Julian date
    // ------------------------------------------------------------
    // Calculation Methods
    private var jafari = 0 // Ithna Ashari
    private var karachi = 1 // University of Islamic Sciences, Karachi
    private var isna = 2 // Islamic Society of North America (ISNA)
    private var mwl = 3 // Muslim World League (MWL)
    private var makkah = 4 // Umm al-Qura, Makkah
    private var egypt = 5 // Egyptian General Authority of Survey
    private var custom = 7 // Custom Setting
    private var tehran = 6 // Institute of Geophysics, University of Tehran
    // Juristic Methods
    private var shafii = 0 // Shafii (standard)
    private var hanafi = 1 // Hanafi
    // Adjusting Methods for Higher Latitudes
    private var none = 0 // No adjustment
    private var midNight = 1 // middle of night
    private var oneSeventh = 2 // 1/7th of night
    private var angleBased = 3 // angle/60th of night
    // Time Formats
    private var time24 = 0 // 24-hour format
    private var time12 = 1 // 12-hour format
    private var time12NS = 2 // 12-hour format with no suffix
    private var floating = 3 // floating point number
    // Time Names
    private val timeNames = arrayListOf(
        "Fajr", "Sunrise", "Dhuhr", "Asr", "Sunset", "Maghrib", "Isha"
    )
    private val invalidTime = "-----" // The string used for invalid times
    // --------------------- Technical Settings --------------------
    private var numIterations = 1 // number of iterations needed to compute times
    // ------------------- Calc Method Parameters --------------------
    private val methodParams: HashMap<Int, DoubleArray> = HashMap()
    private val prayerTimesCurrent: DoubleArray? = null
    private val offsets = intArrayOf(0, 0, 0, 0, 0, 0, 0)

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
        val `val` = asin(x)
        return radiansToDegrees(`val`)
    }

    // degree arcCos
    private fun dArcCos(x: Double): Double {
        val `val` = acos(x)
        return radiansToDegrees(`val`)
    }

    // degree arcTan
    private fun dArcTan(x: Double): Double {
        val `val` = atan(x)
        return radiansToDegrees(`val`)
    }

    // degree arcTan2
    private fun dArcTan2(y: Double, x: Double): Double {
        val `val` = atan2(y, x)
        return radiansToDegrees(`val`)
    }

    // degree arcCot
    private fun dArcCot(x: Double): Double {
        val `val` = atan2(1.0, x)
        return radiansToDegrees(`val`)
    }

    // ---------------------- Time-Zone Functions -----------------------
    // compute local time-zone for a specific date
    private fun getTimeZone(): Double {
        val timez = TimeZone.getDefault()
        return timez.rawOffset / 1000.0 / 3600
    }

    // detect daylight saving in a given date
    private fun detectDaylightSaving(): Double {
        val timez = TimeZone.getDefault()
        return timez.dstSavings.toDouble()
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
        return (floor(365.25 * (year + 4716))
                + floor(30.6001 * (month + 1)) + gDay + b) - 1524.5
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

    // return prayer times for a given date
    fun getPrayerTimes(
        date: Calendar, latitude: Double, longitude: Double, tZone: Double
    ): ArrayList<String> {
        val year = date[Calendar.YEAR]
        val month = date[Calendar.MONTH]
        val day = date[Calendar.DATE]

        val result = getDatePrayerTimes(year, month + 1, day, latitude, longitude, tZone)
        result.removeAt(4)

        for (i in result.indices)
            result[i] = Utils.translateNumbers(context, result[i], true)

        return result
    }

    fun getPrayerTimesArray(
        date: Calendar, latitude: Double, longitude: Double, tZone: Double
    ): Array<Calendar?> {
        val year = date[Calendar.YEAR]
        val month = date[Calendar.MONTH]
        val day = date[Calendar.DATE]
        return toCalendar(
            getDatePrayerTimes(year, month + 1, day, latitude, longitude, tZone)
        )
    }

    private fun toCalendar(givenTimes: ArrayList<String>): Array<Calendar?> {
        val formattedTimes = arrayOfNulls<Calendar>(givenTimes.size - 1) // subtracted one

        // removing sunset time which is the same as maghrib and pushing others
        givenTimes.removeAt(4)
        for (i in givenTimes.indices) {
            val m = givenTimes[i][6]
            var hour = givenTimes[i].substring(0, 2).toInt()
            if (m == 'P' && hour != 12) hour += 12
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
        val str = getDatePrayerTimes(
            year, month + 1, day, latitude,
            longitude, tZone
        )[0]
        var hour = str.substring(0, 2).toInt()
        if (str[6] == 'P') hour += 12
        val calendar = Calendar.getInstance()
        calendar[Calendar.DATE] = day
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = str.substring(3, 5).toInt()
        calendar[Calendar.SECOND] = 0
        return calendar
    }

    /*private fun translateNumbers(subject: ArrayList<String>): ArrayList<String> {
        for (i in subject.indices) {
            val sb = StringBuilder(subject[i])
            subject[i] = sb.toString()
            if (subject[i][0] == '0') subject[i] = subject[i].replaceFirst("0", "")
        }

        for (i in subject.indices) {
            val temp = StringBuilder()
            for (j in 0 until subject[i].length) {
                var t = subject[i][j]
                if (map.containsKey(t)) t = map[t]!!
                temp.append(t)
            }
            subject[i] = temp.toString()
        }
        return subject
    }*/

    // set custom values for calculation parameters
    private fun setCustomParams(params: DoubleArray) {
        for (i in 0..4) {
            if (params[i].toInt() == -1) {
                params[i] = methodParams[calcMethod]?.get(i)!!.toDouble()
                methodParams[custom] = params
            }
            else
                methodParams[custom]?.set(i, params[i])
        }
        calcMethod = custom
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
        if (java.lang.Double.isNaN(time))
            return invalidTime

        time = fixHour(time + 0.5 / 60.0) // add 0.5 minutes to round
        val hours = floor(time).toInt()
        val minutes = floor((time - hours) * 60.0)

        result =
            if (hours in 0..9 && minutes >= 0 && minutes <= 9) "0" + hours + ":0" + minutes.roundToInt()
            else if (hours in 0..9) "0" + hours + ":" + minutes.roundToInt()
            else if (minutes in 0.0..9.0) hours.toString() + ":0" + minutes.roundToInt()
            else hours.toString() + ":" + minutes.roundToInt()

        return result
    }

    // convert double hours to 12h format
    private fun floatToTime12(gTime: Double, noSuffix: Boolean): String {
        var time = gTime
        if (java.lang.Double.isNaN(time))
            return invalidTime

        time = fixHour(time + 0.5 / 60) // add 0.5 minutes to round
        var hours = floor(time).toInt()
        val minutes = floor((time - hours) * 60)
        val result: String
        val suffix: String =
            if (hours >= 12) "PM"
            else "AM"
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
        val asr = computeAsr((1 + asrJuristic).toDouble(), t[3])
        val sunset = computeTime(0.833, t[4])
        val maghrib = computeTime(methodParams[calcMethod]?.get(2)!!, t[5])
        val isha = computeTime(methodParams[calcMethod]?.get(4)!!, t[6])
        return doubleArrayOf(fajr, sunrise, dhuhr, asr, sunset, maghrib, isha)
    }

    // compute prayer times at given julian date
    private fun computeDayTimes(): ArrayList<String> {
        var times = doubleArrayOf(5.0, 6.0, 12.0, 13.0, 18.0, 18.0, 18.0) // default times
        for (i in 1..numIterations)
            times = computeTimes(times)
        adjustTimes(times)
        tuneTimes(times)
        return adjustTimesFormat(times)
    }

    // adjust times in a prayer time array
    private fun adjustTimes(times: DoubleArray): DoubleArray {
        for (i in times.indices)
            times[i] += timeZone - lng / 15

        times[2] += dhuhrMinutes / 60.0 // Dhuhr
        if (methodParams[calcMethod]?.get(1)?.toInt() == 1) // Maghrib
            times[5] = times[4] + (methodParams[calcMethod]?.get(2)!!) / 60
        if (methodParams[calcMethod]?.get(3)?.toInt() == 1) // Isha
            times[6] = times[5] + (methodParams[calcMethod]?.get(4)!!) / 60
        if (adjustHighLats != none)
            adjustHighLatTimes(times)

        return times
    }

    // convert times array to given time format
    private fun adjustTimesFormat(times: DoubleArray): ArrayList<String> {
        val result = ArrayList<String>()
        if (timeFormat == floating) {
            for (time in times)
                result.add(time.toString())
            return result
        }

        for (i in 0..6) {
            when(timeFormat) {
                time12 -> result.add(floatToTime12(times[i], false))
                time12NS -> result.add(floatToTime12(times[i], true))
                else -> result.add(floatToTime24(times[i]))
            }
        }
        return result
    }

    // adjust Fajr, Isha and Maghrib for locations in higher latitudes
    private fun adjustHighLatTimes(times: DoubleArray): DoubleArray {
        val nightTime = timeDiff(times[4], times[1]) // sunset to sunrise

        // Adjust Fajr
        val fajrDiff = nightPortion(
            methodParams[calcMethod]?.get(0)!!
        ) * nightTime
        if (java.lang.Double.isNaN(times[0]) || timeDiff(times[0], times[1]) > fajrDiff) {
            times[0] = times[1] - fajrDiff
        }

        // Adjust Isha
        val ishaAngle: Double =
            if (methodParams[calcMethod]?.get(3)?.toInt() == 0) methodParams[calcMethod]?.get(4)!!.toDouble()
            else 18.0

        val ishaDiff = nightPortion(ishaAngle) * nightTime
        if (java.lang.Double.isNaN(times[6]) || timeDiff(times[4], times[6]) > ishaDiff)
            times[6] = times[4] + ishaDiff

        // Adjust Maghrib
        val maghribAngle: Double =
            if (methodParams[calcMethod]?.get(1)?.toInt() == 0)
                methodParams[calcMethod]?.get(2)!!.toDouble()
            else
                4.0

        val maghribDiff = nightPortion(maghribAngle) * nightTime
        if (java.lang.Double.isNaN(times[5]) || timeDiff(times[4], times[5]) > maghribDiff)
            times[5] = times[4] + maghribDiff

        return times
    }

    // the night portion used for adjusting times in higher latitudes
    private fun nightPortion(angle: Double): Double {
        var calc = 0.0
        when (adjustHighLats) {
            angleBased -> calc = angle / 60.0
            midNight -> calc = 0.5
            oneSeventh -> calc = 0.14286
        }
        return calc
    }

    // convert hours to day portions
    private fun dayPortion(times: DoubleArray): DoubleArray {
        for (i in 0..6)
            times[i] = times[i] / 24
        return times
    }

    // Tune timings for adjustments
    // Set time offsets
    fun tune(offsetTimes: IntArray) {
        System.arraycopy(offsetTimes, 0, offsets, 0, offsetTimes.size)
    }

    private fun tuneTimes(times: DoubleArray): DoubleArray {
        for (i in times.indices) times[i] = times[i] + offsets[i] / 60.0
        return times
    }

    init {
        // Jafari
        val jvalues = doubleArrayOf(16.0, 0.0, 4.0, 0.0, 14.0)
        methodParams[jafari] = jvalues

        // Karachi
        val kvalues = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 18.0)
        methodParams[karachi] = kvalues

        // ISNA
        val ivalues = doubleArrayOf(15.0, 1.0, 0.0, 0.0, 15.0)
        methodParams[isna] = ivalues

        // MWL
        val mwValues = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0)
        methodParams[mwl] = mwValues

        // Makkah
        val mkValues = doubleArrayOf(18.5, 1.0, 0.0, 1.0, 90.0)
        methodParams[makkah] = mkValues

        // Egypt
        val eValues = doubleArrayOf(19.5, 1.0, 0.0, 0.0, 17.5)
        methodParams[egypt] = eValues

        // Tehran
        val tValues = doubleArrayOf(17.7, 0.0, 4.5, 0.0, 14.0)
        methodParams[tehran] = tValues

        // Custom
        val cValues = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0)
        methodParams[custom] = cValues
    }

}