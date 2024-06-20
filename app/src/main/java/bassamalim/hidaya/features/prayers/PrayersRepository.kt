package bassamalim.hidaya.features.prayers

import android.content.res.Resources
import android.location.Location
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.features.prayerSetting.PrayerSettings
import javax.inject.Inject

class PrayersRepository @Inject constructor(
    private val res: Resources,
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    private val language = preferencesDS.getLanguage()
    fun getLanguage() = language
    
    fun getNumeralsLanguage() = preferencesDS.getNumeralsLanguage()

    val dayStr = res.getString(R.string.day)
    val locationFailedStr = res.getString(R.string.location_failed)
    fun getClkToLocateStr() = res.getString(R.string.clk_to_locate)
    fun getHijriMonths(): Array<String> = res.getStringArray(R.array.hijri_months)
    private fun getPrayerNames(): Array<String> = res.getStringArray(R.array.prayer_names)

    fun getCountryID() = preferencesDS.getInt(Preference.CountryID)

    fun getCityID() = preferencesDS.getInt(Preference.CityID)

    fun getLocationType() =
        LocationType.valueOf(preferencesDS.getString(Preference.LocationType))

    fun getClosest(lat: Double, lon: Double) = db.cityDao().getClosest(lat, lon)

    fun getCountryName(countryId: Int): String =
        if (language == Language.ENGLISH) db.countryDao().getNameEn(countryId)
        else db.countryDao().getNameAr(countryId)

    fun getCityName(cityId: Int): String =
        if (language == Language.ENGLISH) db.cityDao().getCity(cityId).nameEn
        else db.cityDao().getCity(cityId).nameAr

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
                    pid = PID.entries[idx],
                    notificationType = notificationTypes[idx],
                    timeOffset = timeOffsets[idx],
                    reminderOffset = reminderOffsets[idx]
                )
            )
        }
    }

    fun updatePrayerSettings(pid: PID, prayerSettings: PrayerSettings) {
        preferencesDS.setString(Preference.NotificationType(pid), prayerSettings.notificationType.name)
        preferencesDS.setInt(Preference.TimeOffset(pid), prayerSettings.timeOffset)
        preferencesDS.setInt(Preference.ReminderOffset(pid), prayerSettings.reminderOffset)
    }

    private fun getNotificationTypes(): List<NotificationType> {
        return listOf(
            NotificationType.valueOf(preferencesDS.getString(Preference.NotificationType(PID.FAJR))),
            NotificationType.valueOf(preferencesDS.getString(Preference.NotificationType(PID.SUNRISE))),
            NotificationType.valueOf(preferencesDS.getString(Preference.NotificationType(PID.DHUHR))),
            NotificationType.valueOf(preferencesDS.getString(Preference.NotificationType(PID.ASR))),
            NotificationType.valueOf(preferencesDS.getString(Preference.NotificationType(PID.MAGHRIB))),
            NotificationType.valueOf(preferencesDS.getString(Preference.NotificationType(PID.ISHAA)))
        )
    }

    private fun getTimeOffsets(): List<Int> =
        PID.entries.map { pid ->
            preferencesDS.getInt(Preference.TimeOffset(pid))
        }

    private fun getReminderOffsets(): List<Int> =
        PID.entries.map { pid ->
            preferencesDS.getInt(Preference.ReminderOffset(pid))
        }

    fun setDoNotShowAgain() {
        preferencesDS.setBoolean(Preference.ShowPrayersTutorial, false)
    }

    fun getShowTutorial() =
        preferencesDS.getBoolean(Preference.ShowPrayersTutorial)

    fun getLocation(): Location? =
        LocUtils.retrieveLocation(preferencesDS.getString(Preference.StoredLocation))

}