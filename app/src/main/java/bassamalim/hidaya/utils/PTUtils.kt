package bassamalim.hidaya.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.preference.PreferenceManager
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.helpers.PrayTimes
import bassamalim.hidaya.other.Global
import java.text.SimpleDateFormat
import java.util.*

object PTUtils {

    fun getTimes(
        context: Context,
        loc: Location? = Keeper(context).retrieveLocation(),
        calendar: Calendar = Calendar.getInstance()
    ): Array<Calendar?>? {
        if (loc == null) return null

        val prayTimes = PrayTimes(context)
        val utcOffset = getUTCOffset(context).toDouble()

        return prayTimes.getPrayerTimes(loc.latitude, loc.longitude, utcOffset, calendar)
    }

    fun getStrTimes(
        context: Context,
        loc: Location? = Keeper(context).retrieveLocation(),
        calendar: Calendar = Calendar.getInstance()
    ): ArrayList<String>? {
        if (loc == null) return null

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val prayTimes = PrayTimes(context)
        val utcOffset = getUTCOffset(context, pref).toDouble()

        val timeFormat = PrefUtils.getTimeFormat(context, pref)

        return prayTimes.getStrPrayerTimes(
            loc.latitude, loc.longitude, utcOffset, calendar, timeFormat
        )
    }

    fun getUTCOffset(
        context: Context,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        db: AppDatabase = DBUtils.getDB(context)
    ): Int {
        when (pref.getString("location_type", "auto")) {
            "auto" -> return TimeZone.getDefault().getOffset(Date().time) / 3600000
            "manual" -> {
                val cityId = pref.getInt("city_id", -1)

                if (cityId == -1) return 0

                val timeZoneId = db.cityDao().getCity(cityId).timeZone

                val timeZone = TimeZone.getTimeZone(timeZoneId)
                return timeZone.getOffset(Date().time) / 3600000
            }
            else -> return 0
        }
    }

    fun cancelAlarm(gContext: Context, PID: PID) {
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            gContext, PID.ordinal, Intent(),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val am: AlarmManager = gContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)

        Log.i(Global.TAG, "Canceled Alarm $PID")
    }

    fun formatTime(context: Context, gStr: String): String {
        var str = gStr

        val hour = "%d".format(str.split(':')[0].toInt())
        var minute = str.split(":")[1]
        minute = minute.replace("am", "")
        minute = minute.replace("pm", "")
        minute = minute.replace("ص", "")
        minute = minute.replace("م", "")
        minute = "%02d".format(minute.toInt())

        str = "$hour:$minute"

        val timeFormat = PrefUtils.getTimeFormat(context)

        val h12Format = SimpleDateFormat("hh:mm aa", Locale.US)
        val h24Format = SimpleDateFormat("HH:mm", Locale.US)

        if (str[str.length-1].isDigit()) {  // Input is in 24h format
            return if (timeFormat == PrayTimes.TF.H24) str
            else {
                val date = h24Format.parse(str)
                val output = h12Format.format(date!!).lowercase()
                output
            }
        }
        else { // Input is in 12h format
            return if (timeFormat == PrayTimes.TF.H12) str
            else {
                val date = h12Format.parse(str)
                val output = h24Format.format(date!!)
                output
            }
        }
    }

    fun mapID(num: Int): PID? {
        return when (num) {
            0 -> PID.FAJR
            1 -> PID.SHOROUQ
            2 -> PID.DUHR
            3 -> PID.ASR
            4 -> PID.MAGHRIB
            5 -> PID.ISHAA
            6 -> PID.MORNING
            7 -> PID.EVENING
            8 -> PID.DAILY_WERD
            9 -> PID.FRIDAY_KAHF
            else -> null
        }
    }

}