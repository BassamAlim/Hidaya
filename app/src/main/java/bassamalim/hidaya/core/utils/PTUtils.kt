package bassamalim.hidaya.core.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.helpers.PrayerTimeCalculator
import bassamalim.hidaya.core.models.Coordinates
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import bassamalim.hidaya.core.other.Global
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.SortedMap
import java.util.TimeZone

object PTUtils {

    fun getTimes(
        settings: PrayerTimeCalculatorSettings,
        timeOffsets: Map<PID, Int>,
        timeZoneId: String = "",
        location: Location,
        calendar: Calendar = Calendar.getInstance()
    ): SortedMap<PID, Calendar?> {
        val prayerTimeCalculator = PrayerTimeCalculator(
            settings = settings,
            timeOffsets = timeOffsets
        )

        val utcOffset = getUTCOffset(location.type, timeZoneId).toDouble()

        return prayerTimeCalculator.getPrayerTimes(
            coordinates = Coordinates(
                location.latitude,
                location.longitude
            ),
            utcOffset = utcOffset,
            calendar = calendar
        )
    }

    fun getFormattedPrayerTimeStrings(
        prayerTimeMap: SortedMap<PID, Calendar?>,
        timeFormat: TimeFormat,
        numeralsLanguage: Language,
    ): SortedMap<PID, String> {
        val formattedTimes = sortedMapOf<PID, String>()
        prayerTimeMap.forEach { (pid, time) ->
            if (time == null) formattedTimes[pid] = ""

            val formattedTime = when (timeFormat) {
                TimeFormat.TWENTY_FOUR -> {
                    val hour = String.format("%02d", time!![Calendar.HOUR_OF_DAY])
                    val minute = String.format("%02d", time[Calendar.MINUTE])
                    "$hour:$minute"
                }
                TimeFormat.TWELVE -> {
                    var hour = time!![Calendar.HOUR_OF_DAY]
                    hour = (hour + 12 - 1) % 12 + 1
                    val minute = time[Calendar.MINUTE]
                    val suffix = if (hour >= 12) "pm" else "am"

                    val formattedHour = String.format("%02d", hour)
                    val formattedMinute = String.format("%02d", minute)
                    "$formattedHour:$formattedMinute $suffix"
                }
            }

            LangUtils.translateNums(
                numeralsLanguage = numeralsLanguage,
                string = formattedTime,
                timeFormat = true
            )
        }
        return formattedTimes
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

    fun formatTime(timeFormat: TimeFormat, string: String): String {
        var str = string

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