package bassamalim.hidaya.core.enums

enum class Prayer {
    FAJR,
    SUNRISE,  // Not a prayer but added for simplifying the code
    DHUHR,
    ASR,
    MAGHRIB,
    ISHAA,
    SUNSET;  // Not a prayer but added for simplifying the code

    fun toReminder() = when (this) {
        FAJR -> Reminder.Prayer.Fajr
        SUNRISE -> Reminder.Prayer.Sunrise
        DHUHR -> Reminder.Prayer.Dhuhr
        ASR -> Reminder.Prayer.Asr
        MAGHRIB -> Reminder.Prayer.Maghrib
        ISHAA -> Reminder.Prayer.Ishaa
        SUNSET -> Reminder.Prayer.Sunrise
    }

    fun toExtraReminder() = when (this) {
        FAJR -> Reminder.PrayerExtra.Fajr
        SUNRISE -> Reminder.PrayerExtra.Sunrise
        DHUHR -> Reminder.PrayerExtra.Dhuhr
        ASR -> Reminder.PrayerExtra.Asr
        MAGHRIB -> Reminder.PrayerExtra.Maghrib
        ISHAA -> Reminder.PrayerExtra.Ishaa
        SUNSET -> Reminder.PrayerExtra.Sunrise
    }

}