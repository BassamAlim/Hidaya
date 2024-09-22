package bassamalim.hidaya.core.utils

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.enums.TimeFormat
import bassamalim.hidaya.core.helpers.PrayerTimeCalculator
import bassamalim.hidaya.core.models.Coordinates
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import java.util.Calendar
import java.util.Date
import java.util.SortedMap
import java.util.TimeZone

object PrayerTimeUtils {

    fun getPrayerTimes(
        settings: PrayerTimeCalculatorSettings,
        timeZoneId: String = "",
        location: Location,
        calendar: Calendar = Calendar.getInstance()
    ) = PrayerTimeCalculator(settings).getPrayerTimes(
        coordinates = Coordinates(location.coordinates.latitude, location.coordinates.longitude),
        utcOffset = getUTCOffset(location.type, timeZoneId).toDouble(),
        calendar = calendar
    )

    fun formatPrayerTimes(
        prayerTimes: SortedMap<Prayer, Calendar?>,
        language: Language,
        numeralsLanguage: Language,
        timeFormat: TimeFormat
    ): SortedMap<Prayer, String> = sortedMapOf<Prayer, String>().apply {
        prayerTimes.forEach { (prayer, time) ->
            put(
                key = prayer,
                value = formatPrayerTime(
                    time = time,
                    language = language,
                    numeralsLanguage = numeralsLanguage,
                    timeFormat = timeFormat
                )
            )
        }
    }

    private fun getUTCOffset(locationType: LocationType, timeZone: String = "") =
        when (locationType) {
            LocationType.AUTO -> TimeZone.getDefault().getOffset(Date().time) / 3600000
            LocationType.MANUAL -> TimeZone.getTimeZone(timeZone).getOffset(Date().time) / 3600000
            LocationType.NONE -> 0
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
                val suffix = when (language) {
                    Language.ENGLISH -> { if (hour >= 12) "pm" else "am" }
                    Language.ARABIC -> { if (hour >= 12) "م" else "ص" }
                }
                hour = (hour + 12 - 1) % 12 + 1
                val minute = time[Calendar.MINUTE]

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