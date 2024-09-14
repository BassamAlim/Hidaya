package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.dataSources.preferences.Preference
import bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.UserPreferences
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Coordinates
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.LocationIds
import bassamalim.hidaya.core.models.MyLocation
import bassamalim.hidaya.core.models.UserRecord
import com.google.gson.Gson

object UserPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames.USER_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: UserPreferences ->
            val storedLocation = sharedPrefs.getString(
                key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.StoredLocation.key,
                defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.StoredLocation.default as String
            )!!

            currentData.copy(
                location =
                    if (storedLocation == "{}") null
                    else {
                        val locationType = LocationType.valueOf(
                            sharedPrefs.getString(
                                key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.LocationType.key,
                                defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.LocationType.default as String
                            )!!
                        )
                        val myLocation = Gson().fromJson(storedLocation, MyLocation::class.java)

                        val countryId = sharedPrefs.getInt(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.CountryID.key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.CountryID.default as Int
                        )
                        val cityId = sharedPrefs.getInt(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.CityID.key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.CityID.default as Int
                        )

                        if (locationType == LocationType.NONE) null
                        else Location(
                            type = locationType,
                            coordinates = Coordinates(
                                latitude = myLocation.latitude,
                                longitude = myLocation.longitude
                            ),
                            ids = LocationIds(
                                countryId = countryId,
                                cityId = cityId
                            )
                        )
                    },
                userRecord = UserRecord(
                    quranPages = sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.QuranPagesRecord.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.QuranPagesRecord.default as Int
                    ),
                    recitationsTime = sharedPrefs.getLong(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.RecitationsPlaybackRecord.key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.RecitationsPlaybackRecord.default as Long
                    )
                ),
            )
        }

}