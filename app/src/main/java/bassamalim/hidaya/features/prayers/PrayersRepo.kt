package bassamalim.hidaya.features.prayers

import android.content.SharedPreferences
import android.content.res.Resources
import android.location.Location
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.core.utils.PrefUtils
import bassamalim.hidaya.features.prayerSetting.PrayerSettings
import javax.inject.Inject

class PrayersRepo @Inject constructor(
    private val res: Resources,
    val sp: SharedPreferences,
    val db: AppDatabase
) {

    val language = PrefUtils.getLanguage(sp)
    val numeralsLanguage = PrefUtils.getNumeralsLanguage(sp)

    fun getCountryID() = PrefUtils.getInt(sp, Prefs.CountryID)

    fun getCityID() = PrefUtils.getInt(sp, Prefs.CityID)

    fun getLocationType() = LocationType.valueOf(
        PrefUtils.getString(sp, Prefs.LocationType)
    )

    fun getClosest(lat: Double, lon: Double) = db.cityDao().getClosest(lat, lon)

    fun getCountryName(countryId: Int): String {
        return if (language == Language.ENGLISH) db.countryDao().getNameEn(countryId)
        else db.countryDao().getNameAr(countryId)
    }

    fun getCityName(cityId: Int): String {
        return if (language == Language.ENGLISH) db.cityDao().getCity(cityId).nameEn
        else db.cityDao().getCity(cityId).nameAr
    }

    fun getPrayersData(): List<PrayerData> {
        val prayerNames = getPrayerNames()
        val notificationTypes = getNotificationTypes()
        val timeOffsets = getTimeOffsets()
        val reminderOffsets = getReminderOffsets()

        return prayerNames.mapIndexed { idx, name ->
            PrayerData(
                name = name,
                time = "",
                settings = PrayerSettings(
                    pid = PID.values()[idx],
                    notificationType = notificationTypes[idx],
                    timeOffset = timeOffsets[idx],
                    reminderOffset = reminderOffsets[idx]
                )
            )
        }
    }

    fun updatePrayerSettings(pid: PID, prayerSettings: PrayerSettings) {
        sp.edit()
            .putString(Prefs.NotificationType(pid).key, prayerSettings.notificationType.name)
            .putInt(Prefs.TimeOffset(pid).key, prayerSettings.timeOffset)
            .putInt(Prefs.ReminderOffset(pid).key, prayerSettings.reminderOffset)
            .apply()
    }

    private fun getNotificationTypes(): List<NotificationType> {
        return listOf(
            NotificationType.valueOf(PrefUtils.getString(sp, Prefs.NotificationType(PID.FAJR))),
            NotificationType.valueOf(PrefUtils.getString(sp, Prefs.NotificationType(PID.SUNRISE))),
            NotificationType.valueOf(PrefUtils.getString(sp, Prefs.NotificationType(PID.DHUHR))),
            NotificationType.valueOf(PrefUtils.getString(sp, Prefs.NotificationType(PID.ASR))),
            NotificationType.valueOf(PrefUtils.getString(sp, Prefs.NotificationType(PID.MAGHRIB))),
            NotificationType.valueOf(PrefUtils.getString(sp, Prefs.NotificationType(PID.ISHAA))),
        )
    }

    private fun getTimeOffsets(): List<Int> {
        return PID.values().map { pid ->
            PrefUtils.getInt(sp, Prefs.TimeOffset(pid))
        }
    }

    private fun getReminderOffsets(): List<Int> {
        return PID.values().map { pid ->
            PrefUtils.getInt(sp, Prefs.ReminderOffset(pid))
        }
    }

    fun setDoNotShowAgain() {
        sp.edit()
            .putBoolean(Prefs.ShowPrayersTutorial.key, false)
            .apply()
    }

    fun getShowTutorial() = PrefUtils.getBoolean(sp, Prefs.ShowPrayersTutorial)

    fun getLocation(): Location? = LocUtils.retrieveLocation(sp)

    fun getHijriMonths(): Array<String> = res.getStringArray(R.array.hijri_months)
    private fun getPrayerNames(): Array<String> = res.getStringArray(R.array.prayer_names)

    fun getDayStr() = res.getString(R.string.day)
    fun getClkToLocate() = res.getString(R.string.clk_to_locate)

}