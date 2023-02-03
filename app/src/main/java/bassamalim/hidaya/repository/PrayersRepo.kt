package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.enums.LocationType
import bassamalim.hidaya.enums.NotificationType
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.utils.LocUtils
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class PrayersRepo @Inject constructor(
    private val context: Context,
    val pref: SharedPreferences,
    val db: AppDatabase
) {

    val language = PrefUtils.getLanguage(pref)
    val numeralsLanguage = PrefUtils.getNumeralsLanguage(pref)

    fun getCountryID() = PrefUtils.getInt(pref, Prefs.CountryID)

    fun getCityID() = PrefUtils.getInt(pref, Prefs.CityID)

    fun getLocationType(): LocationType {
        return LocationType.valueOf(PrefUtils.getString(pref, Prefs.LocationType))
    }

    fun getClosest(lat: Double, lon: Double) = db.cityDao().getClosest(lat, lon)

    fun getPrayerNames(): Array<String> {
        return context.resources.getStringArray(R.array.prayer_names)
    }

    fun getCountryName(countryId: Int): String {
        return if (language == Language.ENGLISH) db.countryDao().getNameEn(countryId)
        else db.countryDao().getNameAr(countryId)
    }

    fun getCityName(cityId: Int): String {
        return if (language == Language.ENGLISH) db.cityDao().getCity(cityId).nameEn
        else db.cityDao().getCity(cityId).nameAr
    }

    fun getTodayText() = context.getString(R.string.day)

    fun getHijriMonths(): Array<String> = context.resources.getStringArray(R.array.hijri_months)

    fun getTimeOffsets(): List<Int> {
        return listOf(
            PrefUtils.getInt(pref, Prefs.TimeOffset(PID.FAJR)),
            PrefUtils.getInt(pref, Prefs.TimeOffset(PID.SUNRISE)),
            PrefUtils.getInt(pref, Prefs.TimeOffset(PID.DHUHR)),
            PrefUtils.getInt(pref, Prefs.TimeOffset(PID.ASR)),
            PrefUtils.getInt(pref, Prefs.TimeOffset(PID.MAGHRIB)),
            PrefUtils.getInt(pref, Prefs.TimeOffset(PID.ISHAA)),
        )
    }

    fun getNotificationTypes(): List<NotificationType> {
        return listOf(
            NotificationType.valueOf(PrefUtils.getString(pref, Prefs.NotificationType(PID.FAJR))),
            NotificationType.valueOf(PrefUtils.getString(pref, Prefs.NotificationType(PID.SUNRISE))),
            NotificationType.valueOf(PrefUtils.getString(pref, Prefs.NotificationType(PID.DHUHR))),
            NotificationType.valueOf(PrefUtils.getString(pref, Prefs.NotificationType(PID.ASR))),
            NotificationType.valueOf(PrefUtils.getString(pref, Prefs.NotificationType(PID.MAGHRIB))),
            NotificationType.valueOf(PrefUtils.getString(pref, Prefs.NotificationType(PID.ISHAA))),
        )
    }

    fun setNotificationType(pid: PID, type: NotificationType) {
        pref.edit()
            .putString(Prefs.NotificationType(pid).key, type.name)
            .apply()
    }

    fun setTimeOffset(pid: PID, offset: Int) {
        pref.edit()
            .putInt(Prefs.TimeOffset(pid).key, offset)
            .apply()
    }

    fun setDoNotShowAgain() {
        pref.edit()
            .putBoolean(Prefs.ShowPrayersTutorial.key, false)
            .apply()
    }

    fun getLocation() = LocUtils.retrieveLocation(pref)

}