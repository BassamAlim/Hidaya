package bassamalim.hidaya.core.enums

enum class Prayer {
    FAJR,
    SUNRISE,  // Not a prayer but added for simplifying the code
    DHUHR,
    ASR,
    SUNSET,  // Not a prayer but added for simplifying the code
    MAGHRIB,
    ISHAA;

    fun toReminder() = when (this) {
        FAJR -> Reminder.Prayer.Fajr
        SUNRISE -> Reminder.Prayer.Sunrise
        DHUHR -> Reminder.Prayer.Dhuhr
        ASR -> Reminder.Prayer.Asr
        SUNSET -> Reminder.Prayer.Sunrise
        MAGHRIB -> Reminder.Prayer.Maghrib
        ISHAA -> Reminder.Prayer.Ishaa
    }

    fun toExtraReminder() = when (this) {
        FAJR -> Reminder.PrayerExtra.Fajr
        SUNRISE -> Reminder.PrayerExtra.Sunrise
        DHUHR -> Reminder.PrayerExtra.Dhuhr
        ASR -> Reminder.PrayerExtra.Asr
        SUNSET -> Reminder.PrayerExtra.Sunrise
        MAGHRIB -> Reminder.PrayerExtra.Maghrib
        ISHAA -> Reminder.PrayerExtra.Ishaa
    }

}