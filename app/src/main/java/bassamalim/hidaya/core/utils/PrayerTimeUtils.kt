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
import java.util.Calendar
import java.util.Date
import java.util.SortedMap
import java.util.TimeZone

object PrayerTimeUtils {

    fun getPrayerTimes(
        settings: PrayerTimeCalculatorSettings,
        timeOffsets: Map<PID, Int>,
        timeZoneId: String = "",
        location: Location,
        calendar: Calendar = Calendar.getInstance()
    ): SortedMap<PID, Calendar?> = PrayerTimeCalculator(
        settings = settings,
        timeOffsets = timeOffsets
    ).getPrayerTimes(
        coordinates = Coordinates(location.latitude, location.longitude),
        utcOffset = getUTCOffset(location.type, timeZoneId).toDouble(),
        calendar = calendar
    )

    fun formatPrayerTimes(
        prayerTimes: SortedMap<PID, Calendar?>,
        language: Language,
        numeralsLanguage: Language,
        timeFormat: TimeFormat
    ): SortedMap<PID, String> = sortedMapOf<PID, String>().apply {
        prayerTimes.forEach { (pid, time) ->
            put(
                key = pid,
                value = formatPrayerTime(
                    time = time,
                    language = language,
                    numeralsLanguage = numeralsLanguage,
                    timeFormat = timeFormat
                )
            )
        }
    }

    fun getUTCOffset(locationType: LocationType, timeZone: String = "") =
        when (locationType) {
            LocationType.AUTO -> TimeZone.getDefault().getOffset(Date().time) / 3600000
            LocationType.MANUAL -> TimeZone.getTimeZone(timeZone).getOffset(Date().time) / 3600000
            LocationType.NONE -> 0
        }

    fun cancelAlarm(context: Context, pid: PID) {
        val pendingIntent = PendingIntent.getBroadcast(
            context, pid.ordinal, Intent(),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Log.i(Global.TAG, "Canceled $pid Alarm")
    }

    fun formatPrayerTime(
        time: Calendar?,
        language: Language,
        numeralsLanguage: Language,
        timeFormat: TimeFormat
    ): String {
        if (time == null) return ""

        val formattedTime = when (timeFormat) {
            TimeFormat.TWENTY_FOUR -> {
                val hour = String.format("%02d", time[Calendar.HOUR_OF_DAY])
                val minute = String.format("%02d", time[Calendar.MINUTE])
                "$hour:$minute"
            }
            TimeFormat.TWELVE -> {
                var hour = time[Calendar.HOUR_OF_DAY]
                hour = (hour + 12 - 1) % 12 + 1
                val minute = time[Calendar.MINUTE]
                val suffix = when (language) {
                    Language.ENGLISH -> { if (hour >= 12) "pm" else "am" }
                    Language.ARABIC -> { if (hour >= 12) "م" else "ص" }
                }

                val formattedHour = String.format("%02d", hour)
                val formattedMinute = String.format("%02d", minute)
                "$formattedHour:$formattedMinute $suffix"
            }
        }

        return LangUtils.translateNums(
            numeralsLanguage = numeralsLanguage,
            string = formattedTime,
            timeFormat = true
        )
    }

}