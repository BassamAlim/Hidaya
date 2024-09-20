package bassamalim.hidaya.core.enums

import kotlinx.serialization.Serializable
import bassamalim.hidaya.core.enums.Prayer as PrayerEnum

@Serializable
sealed class Reminder(val id: Int, val name: String) {

    @Serializable
    sealed class Prayer(
        private val prayerId: Int,
        private val prayerName: String
    ): Reminder(prayerId, prayerName) {
        data object Fajr : Prayer(1, "Fajr")
        data object Sunrise : Prayer(6, "Sunrise")  // Not a prayer, but added for code simplicity
        data object Dhuhr : Prayer(2, "Dhuhr")
        data object Asr : Prayer(3, "Asr")
        data object Maghrib : Prayer(4, "Maghrib")
        data object Ishaa : Prayer(5, "Ishaa")

        fun toPrayer(): PrayerEnum {
            return when (this) {
                Fajr -> PrayerEnum.FAJR
                Sunrise -> PrayerEnum.SUNRISE
                Dhuhr -> PrayerEnum.DHUHR
                Asr -> PrayerEnum.ASR
                Maghrib -> PrayerEnum.MAGHRIB
                Ishaa -> PrayerEnum.ISHAA
            }
        }
    }

    @Serializable
    sealed class PrayerExtra(
        private val prayerId: Int,
        private val prayerName: String
    ): Reminder(prayerId, prayerName) {
        data object Fajr : PrayerExtra(1, "Fajr")
        data object Sunrise : PrayerExtra(6, "Sunrise")  // Not a prayer, but added for code simplicity
        data object Dhuhr : PrayerExtra(2, "Dhuhr")
        data object Asr : PrayerExtra(3, "Asr")
        data object Maghrib : PrayerExtra(4, "Maghrib")
        data object Ishaa : PrayerExtra(5, "Ishaa")

        fun toPrayer(): PrayerEnum {
            return when (this) {
                Fajr -> PrayerEnum.FAJR
                Sunrise -> PrayerEnum.SUNRISE
                Dhuhr -> PrayerEnum.DHUHR
                Asr -> PrayerEnum.ASR
                Maghrib -> PrayerEnum.MAGHRIB
                Ishaa -> PrayerEnum.ISHAA
            }
        }
    }

    @Serializable
    sealed class Devotional(
        private val devotionId: Int,
        private val devotionName: String
    ): Reminder(devotionId, devotionName) {
        data object MorningRemembrances : Devotional(
            devotionId = 9,
            devotionName = "Morning Remembrances"
        )
        data object EveningRemembrances : Devotional(
            devotionId = 10,
            devotionName = "Evening Remembrances"
        )
        data object DailyWerd : Devotional(10, "Daily Werd")
        data object FridayKahf : Devotional(11, "Friday Kahf")
    }

}