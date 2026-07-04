package bassamalim.hidaya.core.utils

import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.models.Coordinates
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.LocationIds
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Covers the timezone-offset handling around the calculator, in particular the DST
 * transition-day regression: the daily update runs at midnight, before the 02:00-03:00
 * transition, and used to compute the whole day with the pre-transition offset, putting
 * every prayer time and athan alarm an hour off on those days.
 */
class PrayerTimeUtilsTest {

    private val berlinZone = "Europe/Berlin"
    private val berlin = Location(
        type = LocationType.MANUAL,
        coordinates = Coordinates(latitude = 52.52, longitude = 13.405, elevation = 0.0),
        ids = LocationIds(countryId = 1, cityId = 1)
    )
    private val settings =
        PrayerTimeCalculatorSettings(calculationMethod = PrayerTimeCalculationMethod.MWL)

    private fun midnightAt(year: Int, month: Int, day: Int, zoneId: String): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone(zoneId)).apply {
            clear()
            set(year, month - 1, day, 0, 0, 0)
        }

    private fun format(calendar: Calendar?): String {
        assertNotNull(calendar)
        return String.format(
            Locale.US,
            "%02d:%02d",
            calendar!![Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE]
        )
    }

    @Test
    fun `spring forward day computed at midnight uses the post-transition offset`() {
        // CEST starts 2024-03-31 at 02:00 (+1h -> +2h); midnight is still on +1h
        val times = PrayerTimeUtils.getPrayerTimes(
            settings = settings,
            selectedTimeZoneId = berlinZone,
            location = berlin,
            calendar = midnightAt(2024, 3, 31, berlinZone)
        )

        // Pre-fix, these came out an hour early (Dhuhr 12:10)
        assertEquals("04:41", format(times[Prayer.FAJR]))
        assertEquals("13:10", format(times[Prayer.DHUHR]))
        assertEquals("19:40", format(times[Prayer.MAGHRIB]))
    }

    @Test
    fun `fall back day computed at midnight uses the post-transition offset`() {
        // CEST ends 2024-10-27 at 03:00 (+2h -> +1h); midnight is still on +2h
        val times = PrayerTimeUtils.getPrayerTimes(
            settings = settings,
            selectedTimeZoneId = berlinZone,
            location = berlin,
            calendar = midnightAt(2024, 10, 27, berlinZone)
        )

        // Pre-fix, these came out an hour late (Dhuhr 12:50)
        assertEquals("04:59", format(times[Prayer.FAJR]))
        assertEquals("11:50", format(times[Prayer.DHUHR]))
        assertEquals("16:45", format(times[Prayer.MAGHRIB]))
    }

    @Test
    fun `result does not depend on the input calendar's time of day`() {
        // September: Berlin has a proper 18-degree twilight, so no prayer is null.
        // In midsummer Fajr/Ishaa would legitimately be null at this latitude.
        val atMidnight = PrayerTimeUtils.getPrayerTimes(
            settings = settings,
            selectedTimeZoneId = berlinZone,
            location = berlin,
            calendar = midnightAt(2024, 9, 15, berlinZone)
        )
        val atNight = PrayerTimeUtils.getPrayerTimes(
            settings = settings,
            selectedTimeZoneId = berlinZone,
            location = berlin,
            calendar = midnightAt(2024, 9, 15, berlinZone).apply {
                this[Calendar.HOUR_OF_DAY] = 22
                this[Calendar.MINUTE] = 30
            }
        )

        for (prayer in atMidnight.keys)
            assertEquals("$prayer", format(atMidnight[prayer]), format(atNight[prayer]))
    }

    @Test
    fun `returned calendars' millis match their wall-clock fields in the real zone`() {
        // What AlarmManager fires on is timeInMillis; it must agree with the displayed time
        val times = PrayerTimeUtils.getPrayerTimes(
            settings = settings,
            selectedTimeZoneId = berlinZone,
            location = berlin,
            calendar = midnightAt(2024, 3, 31, berlinZone)
        )

        for ((prayer, time) in times) {
            assertNotNull("$prayer", time)
            val rendered = Calendar.getInstance(TimeZone.getTimeZone(berlinZone)).apply {
                timeInMillis = time!!.timeInMillis
            }
            assertEquals("$prayer", format(time), format(rendered))
        }
    }

}
