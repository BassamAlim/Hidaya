package bassamalim.hidaya.core.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.helpers.PrayerTimesCalculator
import bassamalim.hidaya.core.models.PrayerTimesCalculatorSettings
import bassamalim.hidaya.core.other.Global
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object PTUtils {

    fun getTimes(
        settings: PrayerTimesCalculatorSettings,
        timeFormat: TimeFormat,
        timeOffsets: Map<PID, Int>,
        numeralsLanguage: Language,
        timeZoneId: String = "",
        locationType: LocationType,
        location: Location,
        calendar: Calendar = Calendar.getInstance()
    ): Array<Calendar?> {
        val prayerTimesCalculator = PrayerTimesCalculator(
            settings,
            timeFormat,
            timeOffsets,
            numeralsLanguage
        )

        val utcOffset = getUTCOffset(locationType, timeZoneId).toDouble()

        return prayerTimesCalculator.getPrayerTimes(
            location.latitude,
            location.longitude,
            utcOffset,
            calendar
        )
    }

    fun getStrTimes(
        settings: PrayerTimesCalculatorSettings,
        timeFormat: TimeFormat,
        timeOffsets: Map<PID, Int>,
        numeralsLanguage: Language,
        locationType: LocationType,
        timeZoneId: String = "",
        location: Location,
        calendar: Calendar = Calendar.getInstance()
    ): ArrayList<String> {
        val prayerTimesCalculator = PrayerTimesCalculator(
            settings,
            timeFormat,
            timeOffsets,
            numeralsLanguage
        )

        val utcOffset = getUTCOffset(locationType, timeZoneId).toDouble()

        return prayerTimesCalculator.getStrPrayerTimes(
            location.latitude,
            location.longitude,
            utcOffset,
            calendar
        )
    }

    fun getUTCOffset(
        locationType: LocationType,
        timeZone: String = "",
    ) = when (locationType) {
        LocationType.AUTO -> TimeZone.getDefault().getOffset(Date().time) / 3600000
        LocationType.MANUAL -> TimeZone.getTimeZone(timeZone).getOffset(Date().time) / 3600000
        LocationType.NONE -> 0
    }

    fun cancelAlarm(context: Context, pid: PID) {
        val pendingIntent = PendingIntent.getBroadcast(
            context, pid.ordinal, Intent(),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val am: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)

        Log.i(Global.TAG, "Canceled $pid Alarm")
    }

    fun formatTime(timeFormat: TimeFormat, gStr: String): String {
        var str = gStr

        val hour = "%d".format(str.split(':')[0].toInt())
        var minute = str.split(":")[1]
        minute = minute.replace("am", "")
        minute = minute.replace("pm", "")
        minute = minute.replace("ص", "")
        minute = minute.replace("م", "")
        minute = "%02d".format(minute.toInt())

        str = "$hour:$minute"

        val h12Format = SimpleDateFormat("hh:mm aa", Locale.US)
        val h24Format = SimpleDateFormat("HH:mm", Locale.US)

        if (str[str.length-1].isDigit()) {  // Input is in 24h format
            return if (timeFormat == TimeFormat.TWENTY_FOUR) str
            else {
                val date = h24Format.parse(str)
                val output = h12Format.format(date!!).lowercase()
                output
            }
        }
        else { // Input is in 12h format
            return if (timeFormat == TimeFormat.TWELVE) str
            else {
                val date = h12Format.parse(str)
                val output = h24Format.format(date!!)
                output
            }
        }
    }

}