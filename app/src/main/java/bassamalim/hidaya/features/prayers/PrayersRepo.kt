package bassamalim.hidaya.features.prayers

import android.content.SharedPreferences
import android.content.res.Resources
import android.location.Location
import bassamalim.hidaya.R
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

    fun getCountryID() = PrefUtils.getInt(sp, bassamalim.hidaya.core.data.Prefs.CountryID)

    fun getCityID() = PrefUtils.getInt(sp, bassamalim.hidaya.core.data.Prefs.CityID)

    fun getLocationType() = LocationType.valueOf(
        PrefUtils.getString(sp, bassamalim.hidaya.core.data.Prefs.LocationType)
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

    fun getNotificationTypes(): List<NotificationType> {
        return listOf(
            NotificationType.valueOf(PrefUtils.getString(sp, bassamalim.hidaya.core.data.Prefs.NotificationType(PID.FAJR))),
            NotificationType.valueOf(PrefUtils.getString(sp, bassamalim.hidaya.core.data.Prefs.NotificationType(PID.SUNRISE))),
            NotificationType.valueOf(PrefUtils.getString(sp, bassamalim.hidaya.core.data.Prefs.NotificationType(PID.DHUHR))),
            NotificationType.valueOf(PrefUtils.getString(sp, bassamalim.hidaya.core.data.Prefs.NotificationType(PID.ASR))),
            NotificationType.valueOf(PrefUtils.getString(sp, bassamalim.hidaya.core.data.Prefs.NotificationType(PID.MAGHRIB))),
            NotificationType.valueOf(PrefUtils.getString(sp, bassamalim.hidaya.core.data.Prefs.NotificationType(PID.ISHAA))),
        )
    }
    fun setNotificationType(pid: PID, type: NotificationType) {
        sp.edit()
            .putString(bassamalim.hidaya.core.data.Prefs.NotificationType(pid).key, type.name)
            .apply()
    }

    fun getTimeOffsets(): List<Int> {
        return listOf(
            PrefUtils.getInt(sp, bassamalim.hidaya.core.data.Prefs.TimeOffset(PID.FAJR)),
            PrefUtils.getInt(sp, bassamalim.hidaya.core.data.Prefs.TimeOffset(PID.SUNRISE)),
            PrefUtils.getInt(sp, bassamalim.hidaya.core.data.Prefs.TimeOffset(PID.DHUHR)),
            PrefUtils.getInt(sp, bassamalim.hidaya.core.data.Prefs.TimeOffset(PID.ASR)),
            PrefUtils.getInt(sp, bassamalim.hidaya.core.data.Prefs.TimeOffset(PID.MAGHRIB)),
            PrefUtils.getInt(sp, bassamalim.hidaya.core.data.Prefs.TimeOffset(PID.ISHAA)),
        )
    }
    fun setTimeOffset(pid: PID, offset: Int) {
        sp.edit()
            .putInt(bassamalim.hidaya.core.data.Prefs.TimeOffset(pid).key, offset)
            .apply()
    }

    fun setDoNotShowAgain() {
        sp.edit()
            .putBoolean(bassamalim.hidaya.core.data.Prefs.ShowPrayersTutorial.key, false)
            .apply()
    }

    fun getShowTutorial() = PrefUtils.getBoolean(sp, bassamalim.hidaya.core.data.Prefs.ShowPrayersTutorial)

    fun getLocation(): Location? = LocUtils.retrieveLocation(sp)

    fun getHijriMonths(): Array<String> = res.getStringArray(R.array.hijri_months)
    fun getPrayerNames(): Array<String> = res.getStringArray(R.array.prayer_names)

    fun getDayStr() = res.getString(R.string.day)
    fun getClkToLocate() = res.getString(R.string.clk_to_locate)

}