package bassamalim.hidaya.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.screens.SettingsScreen
import bassamalim.hidaya.ui.components.ExpandableCard
import bassamalim.hidaya.ui.components.ListPref
import bassamalim.hidaya.ui.components.MyFatColumn
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.PTUtils

class Settings : ComponentActivity() {

    private lateinit var settingsScreen: SettingsScreen
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)

        settingsScreen = SettingsScreen(this)
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    @Composable
    private fun UI() {
        MyScaffold(
            stringResource(R.string.settings)
        ) { padding ->
            Box(
                Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                MyFatColumn {
                    ExpandableCard(
                        R.string.appearance,
                        Modifier.padding(top = 4.dp, bottom = 2.dp)
                    ) {
                        settingsScreen.AppearanceSettings()
                    }

                    ExpandableCard(
                        R.string.extra_notifications,
                        Modifier.padding(vertical = 2.dp)
                    ) {
                        settingsScreen.ExtraNotificationsSettings()
                    }

                    ExpandableCard(
                        R.string.prayer_time_settings,
                        Modifier.padding(vertical = 2.dp)
                    ) {
                        PrayerTimesSettings()
                    }
                }
            }
        }
    }

    @Composable
    fun PrayerTimesSettings() {
        Column(
            Modifier.padding(bottom = 10.dp)
        ) {
            // Calculation method
            ListPref(
                pref = pref,
                titleResId = R.string.calculation_method_title,
                keyResId = R.string.prayer_times_calc_method_key,
                entries = stringArrayResource(R.array.prayer_times_calc_method_entries),
                values = stringArrayResource(R.array.prayer_times_calc_method_values),
                defaultValue = stringResource(R.string.default_prayer_times_calc_method)
            ) {
                val prayerTimes = PTUtils.getTimes(this@Settings)
                if (prayerTimes != null) Alarms(this@Settings, prayerTimes)
            }

            // Juristic method
            ListPref(
                pref = pref,
                titleResId = R.string.juristic_method_title,
                keyResId = R.string.juristic_method_key,
                entries = stringArrayResource(R.array.juristic_method_entries),
                values = stringArrayResource(R.array.juristic_method_values),
                defaultValue = stringResource(R.string.default_juristic_method)
            ) {
                val prayerTimes = PTUtils.getTimes(this@Settings)
                if (prayerTimes != null) Alarms(this@Settings, prayerTimes)
            }

            // High latitude adjustment
            ListPref(
                pref = pref,
                titleResId = R.string.high_lat_adjustment_title,
                keyResId = R.string.high_lat_adjustment_key,
                entries = stringArrayResource(R.array.high_lat_adjustment_entries),
                values = stringArrayResource(R.array.high_lat_adjustment_values),
                defaultValue = stringResource(R.string.default_high_lat_adjustment)
            ) {
                val prayerTimes = PTUtils.getTimes(this@Settings)
                if (prayerTimes != null) Alarms(this@Settings, prayerTimes)
            }
        }
    }

}