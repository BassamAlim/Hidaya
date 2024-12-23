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
import java.util.Locale
import java.util.SortedMap
import java.util.TimeZone

object PrayerTimeUtils {

    fun getPrayerTimes(
        settings: PrayerTimeCalculatorSettings,
        selectedTimeZoneId: String = "",
        location: Location,
        calendar: Calendar = Calendar.getInstance()
    ): SortedMap<Prayer, Calendar?> {
        val coordinates = Coordinates(location.coordinates.latitude, location.coordinates.longitude)
        calendar[Calendar.ZONE_OFFSET] = getZoneOffset(
            locationType = location.type,
            date = calendar.timeInMillis,
            selectedTimeZone = selectedTimeZoneId
        )
        return PrayerTimeCalculator(settings).getPrayerTimes(coordinates, calendar)
    }

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

    private fun getZoneOffset(
        locationType: LocationType,
        date: Long,
        selectedTimeZone: String = ""
    ) = when (locationType) {
        LocationType.AUTO -> TimeZone.getDefault().getOffset(date)
        LocationType.MANUAL -> TimeZone.getTimeZone(selectedTimeZone).getOffset(date)
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
                val hour = String.format(Locale.ENGLISH, "%02d", time[Calendar.HOUR_OF_DAY])
                val minute = String.format(Locale.ENGLISH, "%02d", time[Calendar.MINUTE])
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

                val formattedMinute = String.format(locale = Locale.ENGLISH, "%02d", minute)
                "$hour:$formattedMinute $suffix"
            }
        }

        return LangUtils.translateTimeNums(
            language = language,
            numeralsLanguage = numeralsLanguage,
            string = formattedTime
        )
    }

}