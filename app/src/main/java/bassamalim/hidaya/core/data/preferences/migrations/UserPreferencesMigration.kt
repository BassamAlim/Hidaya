package bassamalim.hidaya.core.data.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.UserPreferences
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.MyLocation
import bassamalim.hidaya.core.models.UserRecord
import com.google.gson.Gson

object UserPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.USER_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: UserPreferences ->
            val storedLocation = sharedPrefs.getString(
                key = Preference.StoredLocation.key,
                defValue = Preference.StoredLocation.default as String
            )!!

            currentData.copy(
                location =
                    if (storedLocation == "{}") null
                    else {
                        val locationType = LocationType.valueOf(
                            sharedPrefs.getString(
                                key = Preference.LocationType.key,
                                defValue = Preference.LocationType.default as String
                            )!!
                        )
                        val myLocation = Gson().fromJson(storedLocation, MyLocation::class.java)

                        val countryId = sharedPrefs.getInt(
                            key = Preference.CountryID.key,
                            defValue = Preference.CountryID.default as Int
                        )
                        val cityId = sharedPrefs.getInt(
                            key = Preference.CityID.key,
                            defValue = Preference.CityID.default as Int
                        )

                        if (locationType == LocationType.NONE) null
                        else Location(
                            type = locationType,
                            latitude = myLocation.latitude,
                            longitude = myLocation.longitude,
                            countryId = countryId,
                            cityId = cityId,
                        )
                    },
                userRecord = UserRecord(
                    quranPages = sharedPrefs.getInt(
                        key = Preference.QuranPagesRecord.key,
                        defValue = Preference.QuranPagesRecord.default as Int
                    ),
                    recitationsTime = sharedPrefs.getLong(
                        key = Preference.TelawatPlaybackRecord.key,
                        defValue = Preference.TelawatPlaybackRecord.default as Long
                    )
                ),
            )
        }

}