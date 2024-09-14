package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.dataSources.preferences.Preference
import bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.preferences.objects.PrayersPreferences
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings
import kotlinx.collections.immutable.persistentMapOf

object PrayersPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames.PRAYERS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: PrayersPreferences ->
            currentData.copy(
                prayerTimeCalculatorSettings = PrayerTimeCalculatorSettings(
                    calculationMethod = PrayerTimeCalculationMethod.valueOf(
                        sharedPrefs.getString(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.PrayerTimesCalculationMethod.key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.PrayerTimesCalculationMethod.default as String
                        )!!
                    ),
                    juristicMethod = PrayerTimeJuristicMethod.valueOf(
                        sharedPrefs.getString(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.PrayerTimesJuristicMethod.key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.PrayerTimesJuristicMethod.default as String
                        )!!
                    ),
                    highLatitudesAdjustmentMethod = HighLatitudesAdjustmentMethod.valueOf(
                        sharedPrefs.getString(
                            key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.PrayerTimesAdjustment.key,
                            defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.PrayerTimesAdjustment.default as String
                        )!!
                    )
                ),
                timeOffsets = persistentMapOf(
                    PID.FAJR to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.FAJR).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.FAJR).default as Int
                    ),
                    PID.SUNRISE to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.SUNRISE).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.SUNRISE).default as Int
                    ),
                    PID.DHUHR to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.DHUHR).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.DHUHR).default as Int
                    ),
                    PID.ASR to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.ASR).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.ASR).default as Int
                    ),
                    PID.MAGHRIB to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.MAGHRIB).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.MAGHRIB).default as Int
                    ),
                    PID.ISHAA to sharedPrefs.getInt(
                        key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.ISHAA).key,
                        defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.TimeOffset(PID.ISHAA).default as Int
                    ),
                ),
                athanAudioId = sharedPrefs.getString(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.AthanId.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.AthanId.default as String
                )!!.toInt(),
                shouldShowTutorial = sharedPrefs.getBoolean(
                    key = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ShowPrayersTutorial.key,
                    defValue = bassamalim.hidaya.core.data.dataSources.preferences.Preference.ShowPrayersTutorial.default as Boolean
                ),
            )
        }

}