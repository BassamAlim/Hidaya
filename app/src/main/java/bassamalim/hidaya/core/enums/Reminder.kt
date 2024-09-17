package bassamalim.hidaya.core.enums

import kotlinx.serialization.Serializable

@Serializable
sealed class Reminder(val id: Int, val name: String) {

    sealed class Prayer(id: Int, name: String): Reminder(id, name) {
        data object Fajr : Prayer(1, "Fajr")
        data object Sunrise : Prayer(6, "Sunrise")
        data object Dhuhr : Prayer(2, "Dhuhr")
        data object Asr : Prayer(3, "Asr")
        data object Maghrib : Prayer(4, "Maghrib")
        data object Ishaa : Prayer(5, "Ishaa")

        fun toPrayerTimePoint(): PrayerTimePoint {
            return when (this) {
                Fajr -> PrayerTimePoint.FAJR
                Sunrise -> PrayerTimePoint.SUNRISE
                Dhuhr -> PrayerTimePoint.DHUHR
                Asr -> PrayerTimePoint.ASR
                Maghrib -> PrayerTimePoint.MAGHRIB
                Ishaa -> PrayerTimePoint.ISHAA
            }
        }
    }

    sealed class Devotional(id: Int, name: String): Reminder(id, name) {
        data object MorningRemembrances : Devotional(9, "Morning Remembrances")
        data object EveningRemembrances : Devotional(10, "Evening Remembrances")
        data object DailyWerd : Devotional(10, "Daily Werd")
        data object FridayKahf : Devotional(11, "Friday Kahf")
    }

}