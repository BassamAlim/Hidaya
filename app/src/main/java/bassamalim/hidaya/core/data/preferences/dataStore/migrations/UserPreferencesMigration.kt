package bassamalim.hidaya.core.data.preferences.dataStore.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.dataStore.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.dataStore.objects.UserPreferences
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.MyLocation
import com.google.gson.Gson

object UserPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.USER_PREFERENCES_NAME,
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

                    when (locationType) {
                        LocationType.Auto -> {
                            Location.FetchedLocation(
                                latitude = myLocation.latitude,
                                longitude = myLocation.longitude
                            )
                        }
                        LocationType.Manual -> {
                            Location.SelectedLocation(
                                countryId = sharedPrefs.getInt(
                                    key = Preference.CountryID.key,
                                    defValue = Preference.CountryID.default as Int
                                ),
                                cityId = sharedPrefs.getInt(
                                    key = Preference.CityID.key,
                                    defValue = Preference.CityID.default as Int
                                ),
                                latitude = myLocation.latitude,
                                longitude = myLocation.longitude
                            )
                        }
                        LocationType.None -> null
                    }
                },
                quranPagesRecord = sharedPrefs.getInt(
                    key = Preference.QuranPagesRecord.key,
                    defValue = Preference.QuranPagesRecord.default as Int
                ),
                recitationsTimeRecord = sharedPrefs.getLong(
                    key = Preference.TelawatPlaybackRecord.key,
                    defValue = Preference.TelawatPlaybackRecord.default as Long
                ),
            )
        }

}