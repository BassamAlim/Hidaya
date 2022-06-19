package bassamalim.hidaya.helpers

import android.content.Context
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import java.lang.StringBuilder
import java.util.*

class PrayTimes(private val context: Context) {
    // ---------------------- Global Variables --------------------
    private var calcMethod // calculation method
            = 0
    private var asrJuristic // Juristic method for Asr
            = 0
    private var dhuhrMinutes // minutes after mid-day for Dhuhr
            = 0
    private var adjustHighLats // adjusting method for higher latitudes
            = 0
    private var timeFormat // time format
            = 0
    private var lat // latitude
            = 0.0
    private var lng // longitude
            = 0.0
    private var timeZone // time-zone
            = 0.0
    private var JDate // Julian date
            = 0.0

    // ------------------------------------------------------------
    // Calculation Methods
    private var Jafari // Ithna Ashari
            = 0
    private var Karachi // University of Islamic Sciences, Karachi
            = 0
    private var ISNA // Islamic Society of North America (ISNA)
            = 0
    private var MWL // Muslim World League (MWL)
            = 0
    private var Makkah // Umm al-Qura, Makkah
            = 0
    private var Egypt // Egyptian General Authority of Survey
            = 0
    private var Custom // Custom Setting
            = 0
    private var Tehran // Institute of Geophysics, University of Tehran
            = 0

    // Juristic Methods
    private var Shafii // Shafii (standard)
            = 0
    private var Hanafi // Hanafi
            = 0

    // Adjusting Methods for Higher Latitudes
    private var None // No adjustment
            = 0
    private var MidNight // middle of night
            = 0
    private var OneSeventh // 1/7th of night
            = 0
    private var AngleBased // angle/60th of night
            = 0

    // Time Formats
    private var Time24 // 24-hour format
            = 0
    private var Time12 // 12-hour format
            = 0
    private var Time12NS // 12-hour format with no suffix
            = 0
    private var Floating // floating point number
            = 0

    // Time Names
    private val timeNames: ArrayList<String>
    private val InvalidTime // The string used for invalid times
            : String

    // --------------------- Technical Settings --------------------
    private var numIterations // number of iterations needed to compute times
            = 0

    // ------------------- Calc Method Parameters --------------------
    private val methodParams: HashMap<Int, DoubleArray>

    /*
     * this.methodParams[methodNum] = new Array(fa, ms, mv, is, iv);
     *
     * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
     * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
     * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter value
     * (in angle or minutes)
     */
    private val prayerTimesCurrent: DoubleArray? = null
    private val offsets: IntArray

    // ---------------------- Trigonometric Functions -----------------------
    // range reduce angle in degrees.
    private fun fixangle(a: Double): Double {
        var a = a
        a = a - 360 * Math.floor(a / 360.0)
        a = if (a < 0) a + 360 else a
        return a
    }

    // range reduce hours to 0..23
    private fun fixhour(a: Double): Double {
        var a = a
        a = a - 24.0 * Math.floor(a / 24.0)
        a = if (a < 0) a + 24 else a
        return a
    }

    // radian to degree
    private fun radiansToDegrees(alpha: Double): Double {
        return alpha * 180.0 / Math.PI
    }

    // deree to radian
    private fun DegreesToRadians(alpha: Double): Double {
        return alpha * Math.PI / 180.0
    }

    // degree sin
    private fun dsin(d: Double): Double {
        return Math.sin(DegreesToRadians(d))
    }

    // degree cos
    private fun dcos(d: Double): Double {
        return Math.cos(DegreesToRadians(d))
    }

    // degree tan
    private fun dtan(d: Double): Double {
        return Math.tan(DegreesToRadians(d))
    }

    // degree arcsin
    private fun darcsin(x: Double): Double {
        val `val` = Math.asin(x)
        return radiansToDegrees(`val`)
    }

    // degree arccos
    private fun darccos(x: Double): Double {
        val `val` = Math.acos(x)
        return radiansToDegrees(`val`)
    }

    // degree arctan
    private fun darctan(x: Double): Double {
        val `val` = Math.atan(x)
        return radiansToDegrees(`val`)
    }

    // degree arctan2
    private fun darctan2(y: Double, x: Double): Double {
        val `val` = Math.atan2(y, x)
        return radiansToDegrees(`val`)
    }

    // degree arccot
    private fun darccot(x: Double): Double {
        val `val` = Math.atan2(1.0, x)
        return radiansToDegrees(`val`)
    }

    // ---------------------- Time-Zone Functions -----------------------
    // compute local time-zone for a specific date
    private fun getTimeZone1(): Double {
        val timez = TimeZone.getDefault()
        return timez.rawOffset / 1000.0 / 3600
    }

    // compute base time-zone of the system
    private fun getBaseTimeZone(): Double {
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
    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var year = year
        var month = month
        if (month <= 2) {
            year -= 1
            month += 12
        }
        val A = Math.floor(year / 100.0)
        val B = 2 - A + Math.floor(A / 4.0)
        return (Math.floor(365.25 * (year + 4716))
                + Math.floor(30.6001 * (month + 1)) + day + B) - 1524.5
    }

    // convert a calendar date to julian date (second method)
    private fun calcJD(year: Int, month: Int, day: Int): Double {
        val J1970 = 2440588.0
        val cal = Calendar.getInstance()
        cal[Calendar.YEAR] = year
        cal[Calendar.MONTH] = month - 1
        cal[Calendar.DATE] = day
        val ms = cal.timeInMillis.toDouble() // # of milliseconds since midnight Jan 1,
        // 1970
        val days = Math.floor(ms / (1000.0 * 60.0 * 60.0 * 24.0))
        return J1970 + days - 0.5
    }

    // ---------------------- Calculation Functions -----------------------
    // References:
    // http://www.ummah.net/astronomy/saltime
    // http://aa.usno.navy.mil/faq/docs/SunApprox.html
    // compute declination angle of sun and equation of time
    private fun sunPosition(jd: Double): DoubleArray {
        val D = jd - 2451545
        val g = fixangle(357.529 + 0.98560028 * D)
        val q = fixangle(280.459 + 0.98564736 * D)
        val L = fixangle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g))

        // double R = 1.00014 - 0.01671 * [self dcos:g] - 0.00014 * [self dcos:
        // (2*g)];
        val e = 23.439 - 0.00000036 * D
        val d = darcsin(dsin(e) * dsin(L))
        var RA = darctan2(dcos(e) * dsin(L), dcos(L)) / 15.0
        RA = fixhour(RA)
        val EqT = q / 15.0 - RA
        val sPosition = DoubleArray(2)
        sPosition[0] = d
        sPosition[1] = EqT
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
    private fun computeMidDay(t: Double): Double {
        val T = equationOfTime(getJDate() + t)
        return fixhour(12 - T)
    }

    // compute time for a given angle G
    private fun computeTime(G: Double, t: Double): Double {
        val D = sunDeclination(getJDate() + t)
        val Z = computeMidDay(t)
        val Beg = -dsin(G) - dsin(D) * dsin(getLat())
        val Mid = dcos(D) * dcos(getLat())
        val V = darccos(Beg / Mid) / 15.0
        return Z + if (G > 90) -V else V
    }

    // compute the time of Asr
    // Shafii: step=1, Hanafi: step=2
    private fun computeAsr(step: Double, t: Double): Double {
        val D = sunDeclination(getJDate() + t)
        val G = -darccot(step + dtan(Math.abs(getLat() - D)))
        return computeTime(G, t)
    }

    // ---------------------- Misc Functions -----------------------
    // compute the difference between two times
    private fun timeDiff(time1: Double, time2: Double): Double {
        return fixhour(time2 - time1)
    }

    // -------------------- Interface Functions --------------------
    // return prayer times for a given date
    private fun getDatePrayerTimes(
        year: Int, month: Int, day: Int,
        latitude: Double, longitude: Double, tZone: Double
    ): ArrayList<String> {
        setLat(latitude)
        setLng(longitude)
        setTimeZone(tZone)
        setJDate(julianDate(year, month, day))
        val lonDiff = longitude / (15.0 * 24.0)
        setJDate(getJDate() - lonDiff)
        return computeDayTimes()
    }

    // return prayer times for a given date
    fun getPrayerTimes(
        date: Calendar, latitude: Double,
        longitude: Double, tZone: Double
    ): ArrayList<String> {
        val year = date[Calendar.YEAR]
        val month = date[Calendar.MONTH]
        val day = date[Calendar.DATE]

        // removing sunset time which is the same as maghrib and pushing others
        val result = translateNumbers(
            getDatePrayerTimes(
                year, month + 1,
                day, latitude, longitude, tZone
            )
        )
        result.removeAt(4)
        return result
    }

    fun getPrayerTimesArray(
        date: Calendar, latitude: Double,
        longitude: Double, tZone: Double
    ): Array<Calendar?> {
        val year = date[Calendar.YEAR]
        val month = date[Calendar.MONTH]
        val day = date[Calendar.DATE]
        return formatTimes(getDatePrayerTimes(year, month + 1, day, latitude, longitude, tZone))
    }

    fun getTomorrowFajr(
        date: Calendar, latitude: Double,
        longitude: Double, tZone: Double
    ): Calendar {
        val year = date[Calendar.YEAR]
        val month = date[Calendar.MONTH]
        val day = date[Calendar.DATE] + 1
        val str = getDatePrayerTimes(
            year, month + 1, day, latitude,
            longitude, tZone
        )[1]
        var hour = str.substring(0, 2).toInt()
        if (str[6] == 'P') hour += 12
        val calendar = Calendar.getInstance()
        calendar[Calendar.DATE] = day
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = str.substring(3, 5).toInt()
        calendar[Calendar.SECOND] = 0
        return calendar
    }

    private fun formatTimes(givenTimes: ArrayList<String>): Array<Calendar?> {
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

    private fun translateNumbers(subject: ArrayList<String>): ArrayList<String> {
        val arabic: Boolean = PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.language_key), context.getString(
                R.string.default_language
            )
        ).equals("ar")
        val map = HashMap<Char, Char>()
        if (!arabic) {
            map['A'] = 'a'
            map['P'] = 'p'
            map['M'] = 'm'
        } else {
            map['0'] = '٠'
            map['1'] = '١'
            map['2'] = '٢'
            map['3'] = '٣'
            map['4'] = '٤'
            map['5'] = '٥'
            map['6'] = '٦'
            map['7'] = '٧'
            map['8'] = '٨'
            map['9'] = '٩'
            map['A'] = 'ص'
            map['P'] = 'م'
        }
        for (i in subject.indices) {
            val sb = StringBuilder(subject[i])
            if (arabic && sb[sb.length - 1] == 'M') sb.deleteCharAt(sb.length - 1)
            subject[i] = sb.toString()
            if (subject[i][0] == '0') subject[i] = subject[i].replaceFirst("0".toRegex(), "")
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
    }

    // set custom values for calculation parameters
    private fun setCustomParams(params: DoubleArray) {
        for (i in 0..4) {
            if (params[i].toInt() == -1) {
                params[i] = methodParams[getCalcMethod()]?.get(i)!!.toDouble()
                methodParams[getCustom()] = params
            } else {
                methodParams[getCustom()]?.set(i, params[i])
            }
        }
        setCalcMethod(getCustom())
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
    fun floatToTime24(time: Double): String {
        var time = time
        val result: String
        if (java.lang.Double.isNaN(time)) {
            return InvalidTime
        }
        time = fixhour(time + 0.5 / 60.0) // add 0.5 minutes to round
        val hours = Math.floor(time).toInt()
        val minutes = Math.floor((time - hours) * 60.0)
        result = if (hours >= 0 && hours <= 9 && minutes >= 0 && minutes <= 9) {
            "0" + hours + ":0" + Math.round(minutes)
        } else if (hours >= 0 && hours <= 9) {
            "0" + hours + ":" + Math.round(minutes)
        } else if (minutes >= 0 && minutes <= 9) {
            hours.toString() + ":0" + Math.round(minutes)
        } else {
            hours.toString() + ":" + Math.round(minutes)
        }
        return result
    }

    // convert double hours to 12h format
    fun floatToTime12(time: Double, noSuffix: Boolean): String {
        var time = time
        if (java.lang.Double.isNaN(time)) {
            return InvalidTime
        }
        time = fixhour(time + 0.5 / 60) // add 0.5 minutes to round
        var hours = Math.floor(time).toInt()
        val minutes = Math.floor((time - hours) * 60)
        val suffix: String
        val result: String
        suffix = if (hours >= 12) {
            "PM"
        } else {
            "AM"
        }
        hours = (hours + 12 - 1) % 12 + 1
        /*hours = (hours + 12) - 1;
        int hrs = (int) hours % 12;
        hrs += 1;*/result = if (!noSuffix) {
            if (hours >= 0 && hours <= 9 && minutes >= 0 && minutes <= 9) {
                ("0" + hours + ":0" + Math.round(minutes) + " "
                        + suffix)
            } else if (hours >= 0 && hours <= 9) {
                "0" + hours + ":" + Math.round(minutes) + " " + suffix
            } else if (minutes >= 0 && minutes <= 9) {
                hours.toString() + ":0" + Math.round(minutes) + " " + suffix
            } else {
                hours.toString() + ":" + Math.round(minutes) + " " + suffix
            }
        } else {
            if (hours >= 0 && hours <= 9 && minutes >= 0 && minutes <= 9) {
                "0" + hours + ":0" + Math.round(minutes)
            } else if (hours >= 0 && hours <= 9) {
                "0" + hours + ":" + Math.round(minutes)
            } else if (minutes >= 0 && minutes <= 9) {
                hours.toString() + ":0" + Math.round(minutes)
            } else {
                hours.toString() + ":" + Math.round(minutes)
            }
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
        val Fajr = computeTime(
            180 - (methodParams[getCalcMethod()]?.get(0)!!), t[0]
        )
        val Sunrise = computeTime(180 - 0.833, t[1])
        val Dhuhr = computeMidDay(t[2])
        val Asr = computeAsr((1 + getAsrJuristic()).toDouble(), t[3])
        val Sunset = computeTime(0.833, t[4])
        val Maghrib = computeTime(
            methodParams[getCalcMethod()]?.get(2)!!, t[5]
        )
        val Isha = computeTime(
            methodParams[getCalcMethod()]?.get(4)!!, t[6]
        )
        return doubleArrayOf(Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha)
    }

    // compute prayer times at given julian date
    private fun computeDayTimes(): ArrayList<String> {
        var times = doubleArrayOf(5.0, 6.0, 12.0, 13.0, 18.0, 18.0, 18.0) // default times
        for (i in 1..getNumIterations()) {
            times = computeTimes(times)
        }
        adjustTimes(times)
        tuneTimes(times)
        return adjustTimesFormat(times)
    }

    // adjust times in a prayer time array
    private fun adjustTimes(times: DoubleArray): DoubleArray {
        for (i in times.indices) {
            times[i] += getTimeZone() - getLng() / 15
        }
        times[2] += getDhuhrMinutes() / 60.0 // Dhuhr
        if (methodParams[getCalcMethod()]?.get(1)?.toInt() == 1) // Maghrib
        {
            times[5] = times[4] + (methodParams[getCalcMethod()]?.get(2)!!) / 60
        }
        if (methodParams[getCalcMethod()]?.get(3)?.toInt() == 1) // Isha
        {
            times[6] = times[5] + (methodParams[getCalcMethod()]?.get(4)!!) / 60
        }
        if (getAdjustHighLats() != getNone()) {
            adjustHighLatTimes(times)
        }
        return times
    }

    // convert times array to given time format
    private fun adjustTimesFormat(times: DoubleArray): ArrayList<String> {
        val result = ArrayList<String>()
        if (getTimeFormat() == getFloating()) {
            for (time in times) {
                result.add(time.toString())
            }
            return result
        }
        for (i in 0..6) {
            if (getTimeFormat() == getTime12()) {
                result.add(floatToTime12(times[i], false))
            } else if (getTimeFormat() == getTime12NS()) {
                result.add(floatToTime12(times[i], true))
            } else {
                result.add(floatToTime24(times[i]))
            }
        }
        return result
    }

    // adjust Fajr, Isha and Maghrib for locations in higher latitudes
    private fun adjustHighLatTimes(times: DoubleArray): DoubleArray {
        val nightTime = timeDiff(times[4], times[1]) // sunset to sunrise

        // Adjust Fajr
        val FajrDiff = nightPortion(
            methodParams[getCalcMethod()]?.get(0)!!
        ) * nightTime
        if (java.lang.Double.isNaN(times[0]) || timeDiff(times[0], times[1]) > FajrDiff) {
            times[0] = times[1] - FajrDiff
        }

        // Adjust Isha
        val IshaAngle: Double = if (
            methodParams[getCalcMethod()]?.get(3)?.toInt() == 0
        ) methodParams[getCalcMethod()]?.get(4)!!.toDouble() else 18.0
        val IshaDiff = nightPortion(IshaAngle) * nightTime
        if (java.lang.Double.isNaN(times[6]) || timeDiff(times[4], times[6]) > IshaDiff) {
            times[6] = times[4] + IshaDiff
        }

        // Adjust Maghrib
        val MaghribAngle: Double = if (
            methodParams[getCalcMethod()]?.get(1)?.toInt() == 0
        ) methodParams[getCalcMethod()]?.get(2)!!.toDouble() else 4.0
        val MaghribDiff = nightPortion(MaghribAngle) * nightTime
        if (java.lang.Double.isNaN(times[5]) || timeDiff(times[4], times[5]) > MaghribDiff) {
            times[5] = times[4] + MaghribDiff
        }
        return times
    }

    // the night portion used for adjusting times in higher latitudes
    private fun nightPortion(angle: Double): Double {
        var calc = 0.0
        if (adjustHighLats == AngleBased) calc =
            angle / 60.0 else if (adjustHighLats == MidNight) calc =
            0.5 else if (adjustHighLats == OneSeventh) calc = 0.14286
        return calc
    }

    // convert hours to day portions
    private fun dayPortion(times: DoubleArray): DoubleArray {
        for (i in 0..6) {
            times[i] = times[i] / 24
        }
        return times
    }

    // Tune timings for adjustments
    // Set time offsets
    fun tune(offsetTimes: IntArray) {
        System.arraycopy(offsetTimes, 0, offsets, 0, offsetTimes.size)
    }

    private fun tuneTimes(times: DoubleArray): DoubleArray {
        for (i in times.indices) {
            times[i] = times[i] + offsets[i] / 60.0
        }
        return times
    }

    /*public static void main(String[] args) {
        double latitude = 21.622074;
        double longitude = 39.132218;
        double timezone = 3;
        // Test Prayer times here
        PrayTimes prayers = new PrayTimes();

        prayers.setTimeFormat(prayers.Time12);
        prayers.setCalcMethod(prayers.Makkah);
        prayers.setAsrJuristic(prayers.Shafii);
        prayers.setAdjustHighLats(prayers.AngleBased);
        int[] offsets = {0, 0, 0, 0, 0, 0, 0}; // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
        prayers.tune(offsets);

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        ArrayList<String> prayerTimes = prayers.getPrayerTimes(cal,
                latitude, longitude, timezone);
        ArrayList<String> prayerNames = prayers.getTimeNames();

        for (int i = 0; i < prayerTimes.size(); i++) {
            System.out.println(prayerNames.get(i) + " - " + prayerTimes.get(i));
        }

    }*/
    fun getCalcMethod(): Int {
        return calcMethod
    }

    fun setCalcMethod(calcMethod: Int) {
        this.calcMethod = calcMethod
    }

    fun getAsrJuristic(): Int {
        return asrJuristic
    }

    fun setAsrJuristic(asrJuristic: Int) {
        this.asrJuristic = asrJuristic
    }

    fun getDhuhrMinutes(): Int {
        return dhuhrMinutes
    }

    fun setDhuhrMinutes(dhuhrMinutes: Int) {
        this.dhuhrMinutes = dhuhrMinutes
    }

    fun getAdjustHighLats(): Int {
        return adjustHighLats
    }

    fun setAdjustHighLats(adjustHighLats: Int) {
        this.adjustHighLats = adjustHighLats
    }

    fun getTimeFormat(): Int {
        return timeFormat
    }

    fun setTimeFormat(timeFormat: Int) {
        this.timeFormat = timeFormat
    }

    fun getLat(): Double {
        return lat
    }

    fun setLat(lat: Double) {
        this.lat = lat
    }

    fun getLng(): Double {
        return lng
    }

    fun setLng(lng: Double) {
        this.lng = lng
    }

    fun getTimeZone(): Double {
        return timeZone
    }

    fun setTimeZone(timeZone: Double) {
        this.timeZone = timeZone
    }

    fun getJDate(): Double {
        return JDate
    }

    fun setJDate(jDate: Double) {
        JDate = jDate
    }

    private fun getJafari(): Int {
        return Jafari
    }

    private fun setJafari(jafari: Int) {
        Jafari = jafari
    }

    private fun getKarachi(): Int {
        return Karachi
    }

    private fun setKarachi(karachi: Int) {
        Karachi = karachi
    }

    private fun getISNA(): Int {
        return ISNA
    }

    private fun setISNA(iSNA: Int) {
        ISNA = iSNA
    }

    private fun getMWL(): Int {
        return MWL
    }

    private fun setMWL(mWL: Int) {
        MWL = mWL
    }

    private fun getMakkah(): Int {
        return Makkah
    }

    private fun setMakkah(makkah: Int) {
        Makkah = makkah
    }

    private fun getEgypt(): Int {
        return Egypt
    }

    private fun setEgypt(egypt: Int) {
        Egypt = egypt
    }

    private fun getCustom(): Int {
        return Custom
    }

    private fun setCustom(custom: Int) {
        Custom = custom
    }

    private fun getTehran(): Int {
        return Tehran
    }

    private fun setTehran(tehran: Int) {
        Tehran = tehran
    }

    private fun getShafii(): Int {
        return Shafii
    }

    private fun setShafii(shafii: Int) {
        Shafii = shafii
    }

    private fun getHanafi(): Int {
        return Hanafi
    }

    private fun setHanafi(hanafi: Int) {
        Hanafi = hanafi
    }

    private fun getNone(): Int {
        return None
    }

    private fun setNone(none: Int) {
        None = none
    }

    private fun getMidNight(): Int {
        return MidNight
    }

    private fun setMidNight(midNight: Int) {
        MidNight = midNight
    }

    private fun getOneSeventh(): Int {
        return OneSeventh
    }

    private fun setOneSeventh(oneSeventh: Int) {
        OneSeventh = oneSeventh
    }

    private fun getAngleBased(): Int {
        return AngleBased
    }

    private fun setAngleBased(angleBased: Int) {
        AngleBased = angleBased
    }

    private fun getTime24(): Int {
        return Time24
    }

    private fun setTime24(time24: Int) {
        Time24 = time24
    }

    private fun getTime12(): Int {
        return Time12
    }

    private fun setTime12(time12: Int) {
        Time12 = time12
    }

    private fun getTime12NS(): Int {
        return Time12NS
    }

    private fun setTime12NS(time12ns: Int) {
        Time12NS = time12ns
    }

    private fun getFloating(): Int {
        return Floating
    }

    private fun setFloating(floating: Int) {
        Floating = floating
    }

    private fun getNumIterations(): Int {
        return numIterations
    }

    private fun setNumIterations(numIterations: Int) {
        this.numIterations = numIterations
    }

    fun getTimeNames(): ArrayList<String> {
        return timeNames
    }

    init {

        // Initialize vars
        setCalcMethod(4)
        setAsrJuristic(0)
        setDhuhrMinutes(0)
        setAdjustHighLats(0)
        setTimeFormat(1)

        // Calculation Methods
        setJafari(0) // Ithna Ashari
        setKarachi(1) // University of Islamic Sciences, Karachi
        setISNA(2) // Islamic Society of North America (ISNA)
        setMWL(3) // Muslim World League (MWL)
        setMakkah(4) // Umm al-Qura, Makkah
        setEgypt(5) // Egyptian General Authority of Survey
        setTehran(6) // Institute of Geophysics, University of Tehran
        setCustom(7) // Custom Setting

        // Juristic Methods
        setShafii(0) // Shafii (standard)
        setHanafi(1) // Hanafi

        // Adjusting Methods for Higher Latitudes
        setNone(0) // No adjustment
        setMidNight(1) // middle of night
        setOneSeventh(2) // 1/7th of night
        setAngleBased(3) // angle/60th of night

        // Time Formats
        setTime24(0) // 24-hour format
        setTime12(1) // 12-hour format
        setTime12NS(2) // 12-hour format with no suffix
        setFloating(3) // floating point number

        // Time Names
        timeNames = ArrayList()
        timeNames.add("Fajr")
        timeNames.add("Sunrise")
        timeNames.add("Dhuhr")
        timeNames.add("Asr")
        timeNames.add("Sunset")
        timeNames.add("Maghrib")
        timeNames.add("Isha")
        InvalidTime = "-----" // The string used for invalid times

        // --------------------- Technical Settings --------------------
        setNumIterations(1) // number of iterations needed to compute
        // times

        // ------------------- Calc Method Parameters --------------------

        // Tuning offsets {fajr, sunrise, dhuhr, asr, sunset, maghrib, isha}
        offsets = IntArray(7)
        offsets[0] = 0
        offsets[1] = 0
        offsets[2] = 0
        offsets[3] = 0
        offsets[4] = 0
        offsets[5] = 0
        offsets[6] = 0

        /*
         *
         * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
         * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
         * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter
         * value (in angle or minutes)
         */methodParams = HashMap()

        // Jafari
        val Jvalues = doubleArrayOf(16.0, 0.0, 4.0, 0.0, 14.0)
        methodParams[getJafari()] = Jvalues

        // Karachi
        val Kvalues = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 18.0)
        methodParams[getKarachi()] = Kvalues

        // ISNA
        val Ivalues = doubleArrayOf(15.0, 1.0, 0.0, 0.0, 15.0)
        methodParams[getISNA()] = Ivalues

        // MWL
        val MWvalues = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0)
        methodParams[getMWL()] = MWvalues

        // Makkah
        val MKvalues = doubleArrayOf(18.5, 1.0, 0.0, 1.0, 90.0)
        methodParams[getMakkah()] = MKvalues

        // Egypt
        val Evalues = doubleArrayOf(19.5, 1.0, 0.0, 0.0, 17.5)
        methodParams[getEgypt()] = Evalues

        // Tehran
        val Tvalues = doubleArrayOf(17.7, 0.0, 4.5, 0.0, 14.0)
        methodParams[getTehran()] = Tvalues

        // Custom
        val Cvalues = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0)
        methodParams[getCustom()] = Cvalues
    }
}