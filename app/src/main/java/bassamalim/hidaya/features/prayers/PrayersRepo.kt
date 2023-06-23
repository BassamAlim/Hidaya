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

        return PID.values().mapIndexed { idx, pid ->
            PrayerData(
                pid = pid,
                name = prayerNames[idx],
                time = "",
                notificationType = notificationTypes[idx],
                timeOffset = timeOffsets[idx],
                reminderOffset = reminderOffsets[idx]
            )
        }
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
    fun setNotificationType(pid: PID, type: NotificationType) {
        sp.edit()
            .putString(Prefs.NotificationType(pid).key, type.name)
            .apply()
    }

    private fun getTimeOffsets(): List<Int> {
        return PID.values().map { pid ->
            PrefUtils.getInt(sp, Prefs.TimeOffset(pid))
        }
    }
    fun setTimeOffset(pid: PID, offset: Int) {
        sp.edit()
            .putInt(Prefs.TimeOffset(pid).key, offset)
            .apply()
    }

    private fun getReminderOffsets(): List<Int> {
        return PID.values().map { pid ->
            PrefUtils.getInt(sp, Prefs.ReminderOffset(pid))
        }
    }
    fun setReminderOffset(pid: PID, offset: Int) {
        sp.edit()
            .putInt(Prefs.ReminderOffset(pid).key, offset)
            .apply()
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