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
        @Serializable
        data object Fajr : Prayer(1, "Fajr")
        @Serializable
        data object Sunrise : Prayer(2, "Sunrise")  // Not a prayer, but added for code simplicity
        @Serializable
        data object Dhuhr : Prayer(3, "Dhuhr")
        @Serializable
        data object Asr : Prayer(4, "Asr")
        @Serializable
        data object Maghrib : Prayer(5, "Maghrib")
        @Serializable
        data object Ishaa : Prayer(6, "Ishaa")

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

        fun toPrayerExtra(): PrayerExtra {
            return when (this) {
                Fajr -> PrayerExtra.Fajr
                Sunrise -> PrayerExtra.Sunrise
                Dhuhr -> PrayerExtra.Dhuhr
                Asr -> PrayerExtra.Asr
                Maghrib -> PrayerExtra.Maghrib
                Ishaa -> PrayerExtra.Ishaa
            }
        }
    }

    @Serializable
    sealed class PrayerExtra(
        private val prayerId: Int,
        private val prayerName: String
    ): Reminder(prayerId, prayerName) {
        @Serializable
        data object Fajr : PrayerExtra(7, "Fajr Extra")
        @Serializable
        data object Sunrise : PrayerExtra(8, "Sunrise Extra")  // Not a prayer, but added for code simplicity
        @Serializable
        data object Dhuhr : PrayerExtra(9, "Dhuhr Extra")
        @Serializable
        data object Asr : PrayerExtra(10, "Asr Extra")
        @Serializable
        data object Maghrib : PrayerExtra(11, "Maghrib Extra")
        @Serializable
        data object Ishaa : PrayerExtra(12, "Ishaa Extra")

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
        @Serializable
        data object MorningRemembrances : Devotional(
            devotionId = 13,
            devotionName = "Morning Remembrances"
        )
        @Serializable
        data object EveningRemembrances : Devotional(
            devotionId = 14,
            devotionName = "Evening Remembrances"
        )
        @Serializable
        data object DailyWerd : Devotional(15, "Daily Werd")
        @Serializable
        data object FridayKahf : Devotional(16, "Friday Kahf")
    }

    companion object {
        fun getById(id: Int): Reminder {
            return when (id) {
                1 -> Prayer.Fajr
                2 -> Prayer.Sunrise
                3 -> Prayer.Dhuhr
                4 -> Prayer.Asr
                5 -> Prayer.Maghrib
                6 -> Prayer.Ishaa
                7 -> PrayerExtra.Fajr
                8 -> PrayerExtra.Sunrise
                9 -> PrayerExtra.Dhuhr
                10 -> PrayerExtra.Asr
                11 -> PrayerExtra.Maghrib
                12 -> PrayerExtra.Ishaa
                13 -> Devotional.MorningRemembrances
                14 -> Devotional.EveningRemembrances
                15 -> Devotional.DailyWerd
                16 -> Devotional.FridayKahf
                else -> throw IllegalArgumentException("Invalid Reminder ID")
            }
        }
    }

}