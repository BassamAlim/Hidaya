package bassamalim.hidaya.core.data.dataSources.preferences.migrations

import android.content.Context
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import bassamalim.hidaya.core.data.dataSources.preferences.Preference
import bassamalim.hidaya.core.data.dataSources.preferences.PreferencesFileNames
import bassamalim.hidaya.core.data.dataSources.preferences.objects.PrayersPreferences
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.models.PrayerTimeCalculatorSettings

object PrayersPreferencesMigration {

    fun getMigration(context: Context) =
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = PreferencesFileNames.PRAYERS_PREFERENCES_NAME
        ) { sharedPrefs: SharedPreferencesView, currentData: PrayersPreferences ->
            currentData.copy(
                prayerTimeCalculatorSettings = PrayerTimeCalculatorSettings(
                    calculationMethod = PrayerTimeCalculationMethod.valueOf(
                        sharedPrefs.getString(
                            key = Preference.PrayerTimesCalculationMethod.key,
                            defValue = Preference.PrayerTimesCalculationMethod.default as String
                        )!!
                    ),
                    juristicMethod = PrayerTimeJuristicMethod.valueOf(
                        sharedPrefs.getString(
                            key = Preference.PrayerTimesJuristicMethod.key,
                            defValue = Preference.PrayerTimesJuristicMethod.default as String
                        )!!
                    ),
                    highLatitudesAdjustmentMethod = HighLatitudesAdjustmentMethod.valueOf(
                        sharedPrefs.getString(
                            key = Preference.PrayerTimesAdjustment.key,
                            defValue = Preference.PrayerTimesAdjustment.default as String
                        )!!
                    )
                ),
                athanAudioId = sharedPrefs.getString(
                    key = Preference.AthanId.key,
                    defValue = Preference.AthanId.default as String
                )!!.toInt(),
                shouldShowTutorial = sharedPrefs.getBoolean(
                    key = Preference.ShowPrayersTutorial.key,
                    defValue = Preference.ShowPrayersTutorial.default as Boolean
                ),
            )
        }

}