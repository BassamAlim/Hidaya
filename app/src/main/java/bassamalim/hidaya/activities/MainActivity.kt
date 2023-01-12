package bassamalim.hidaya.activities

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.dialogs.DateEditorDialog
import bassamalim.hidaya.enum.Language
import bassamalim.hidaya.enum.Theme
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.receivers.DailyUpdateReceiver
import bassamalim.hidaya.receivers.DeviceBootReceiver
import bassamalim.hidaya.screens.*
import bassamalim.hidaya.ui.components.BottomNavItem
import bassamalim.hidaya.ui.components.MyBottomNavigation
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.PrefUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    private lateinit var theme: Theme
    private lateinit var language: Language
    private var navScreen: NavigationScreen? = null
    private val dateOffset = mutableStateOf(0)

    companion object {
        var location: Location? = null
        var located = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        theme = ActivityUtils.onActivityCreateSetTheme(this)
        language = ActivityUtils.onActivityCreateSetLocale(this)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        dateOffset.value = PrefUtils.getInt(pref, Prefs.DateOffset)

        getLocation()

        setContent {
            AppTheme {
                MainUI()
            }
        }

        initFirebase()

        dailyUpdate()

        setAlarms()

        setupBootReceiver()
    }

    override fun onRestart() {
        super.onRestart()

        val newTheme = PrefUtils.getTheme(pref)
        val newLanguage = PrefUtils.getLanguage(pref)

        if (newTheme != theme || newLanguage != language) {
            theme = newTheme
            language = newLanguage

            ActivityUtils.restartActivity(this)
        }
    }

    override fun onPause() {
        super.onPause()
        navScreen?.onPause()
    }

    override fun onResume() {
        super.onResume()
        navScreen?.onResume()
    }

    private fun getLocation() {
        val intent = intent
        located = intent.getBooleanExtra("located", false)
        if (located) {
            location =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    intent.getParcelableExtra("location", Location::class.java)
                else
                    intent.getParcelableExtra("location")
        }
    }

    private fun initFirebase() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                                    // update at most every six hours
            .setMinimumFetchIntervalInSeconds(3600 * 6).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    private fun setAlarms() {
        if (located) {
            Keeper(this, location!!)
            Alarms(this, PTUtils.getTimes(this, location!!)!!)
        }
        else
            Toast.makeText(
                this, getString(R.string.give_location_permission_toast),
                Toast.LENGTH_SHORT
            ).show()
    }

    private fun dailyUpdate() {
        val intent = Intent(this, DailyUpdateReceiver::class.java)
        intent.action = "daily"
        sendBroadcast(intent)
    }

    private fun getTodayScreenContent(dateOffset: MutableState<Int>): Array<String> {
        val hijri = UmmalquraCalendar()
        val hDayName = resources.getStringArray(R.array.week_days)[hijri[Calendar.DAY_OF_WEEK] - 1]

        val millisInDay = 1000 * 60 * 60 * 24
        hijri.timeInMillis = hijri.timeInMillis + dateOffset.value * millisInDay

        val hMonth = resources.getStringArray(R.array.hijri_months)[hijri[Calendar.MONTH]]
        var hijriStr = "$hDayName ${hijri[Calendar.DATE]} $hMonth ${hijri[Calendar.YEAR]}"
        hijriStr = LangUtils.translateNums(this, hijriStr, false)

        val gregorian = Calendar.getInstance()
        val mMonth = resources.getStringArray(R.array.gregorian_months)[gregorian[Calendar.MONTH]]
        var gregorianStr = "${gregorian[Calendar.DATE]} $mMonth ${gregorian[Calendar.YEAR]}"
        gregorianStr = LangUtils.translateNums(this, gregorianStr, false)
        
        return arrayOf(hijriStr, gregorianStr)
    }

    private fun setupBootReceiver() {
        applicationContext.packageManager.setComponentEnabledSetting(
            ComponentName(this, DeviceBootReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    @Composable
    fun MainUI() {
        val navController = rememberNavController()
        val dateEditorShown = remember { mutableStateOf(false) }

        MyScaffold(
            title = getString(R.string.app_name),
            topBar = {
                TopAppBar(
                    backgroundColor = AppTheme.colors.primary,
                    elevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        Modifier.fillMaxSize()
                    ) {
                        Row(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MyText(
                                stringResource(R.string.app_name),
                                textColor = AppTheme.colors.onPrimary
                            )

                            Column(
                                Modifier
                                    .fillMaxHeight()
                                    .clickable { dateEditorShown.value = true },
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxHeight()
                                        .padding(horizontal = 10.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    val content = getTodayScreenContent(dateOffset)

                                    MyText(
                                        text = content[0],
                                        fontSize = 16.nsp,
                                        fontWeight = FontWeight.Bold,
                                        textColor = AppTheme.colors.onPrimary
                                    )

                                    MyText(
                                        text = content[1],
                                        fontSize = 16.nsp,
                                        textColor = AppTheme.colors.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = { MyBottomNavigation(navController) }
        ) {
            NavigationGraph(navController, it)

            if (dateEditorShown.value)
                DateEditorDialog(this, pref, dateOffset, dateEditorShown).Dialog()
        }
    }

    @Composable
    fun NavigationGraph(navController: NavHostController, padding: PaddingValues) {
        NavHost(
            navController,
            startDestination = BottomNavItem.Home.screen_route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.screen_route) {
                navScreen = HomeScreen(this@MainActivity, pref, located, location)
                (navScreen as HomeScreen).HomeUI()
            }
            composable(BottomNavItem.Prayers.screen_route) {
                navScreen = PrayersScreen(this@MainActivity, pref, located, location)
                (navScreen as PrayersScreen).PrayersUI()
            }
            composable(BottomNavItem.Quran.screen_route) {
                navScreen = QuranScreen(this@MainActivity, pref)
                (navScreen as QuranScreen).QuranUI()
            }
            composable(BottomNavItem.Athkar.screen_route) {
                navScreen = AthkarScreen(this@MainActivity)
                (navScreen as AthkarScreen).AthkarUI()
            }
            composable(BottomNavItem.More.screen_route) {
                navScreen = MoreScreen(this@MainActivity)
                (navScreen as MoreScreen).MoreUI()
            }
        }
    }

}