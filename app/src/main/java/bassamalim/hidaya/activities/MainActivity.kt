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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.dialogs.DateEditorDialog
import bassamalim.hidaya.screens.*
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.other.DialogCallback
import bassamalim.hidaya.receivers.DailyUpdateReceiver
import bassamalim.hidaya.receivers.DeviceBootReceiver
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.PrefUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.*

class MainActivity : AppCompatActivity() {

    private var remoteConfig: FirebaseRemoteConfig? = FirebaseRemoteConfig.getInstance()
    private lateinit var pref: SharedPreferences
    private lateinit var language: String
    private lateinit var theme: String
    private var times: Array<Calendar?>? = null
    private var currentScreen = mutableStateOf("")

    private var homeScreen: HomeScreen? = null
    private var prayersScreen: PrayersScreen? = null
    private var quranScreen: QuranScreen? = null
    private var athkarScreen: AthkarScreen? = null
    private var moreScreen: MoreScreen? = null

    companion object {
        var location: Location? = null
        var located = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.onActivityCreateSetTheme(this)
        language = ActivityUtils.onActivityCreateSetLocale(this)

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        theme = PrefUtils.getTheme(this, pref)

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

        val newTheme = PrefUtils.getTheme(this, pref)
        val newLanguage = PrefUtils.getLanguage(this, pref)

        if (newTheme != theme || newLanguage != language) {
            theme = newTheme
            language = newLanguage

            ActivityUtils.restartActivity(this)
        }

        getTodayScreenContent()
    }

    override fun onPause() {
        super.onPause()
        homeScreen?.onPause()
    }

    override fun onResume() {
        super.onResume()
        homeScreen?.onResume()
        quranScreen?.onResume()
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
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                                    // update at most every six hours
            .setMinimumFetchIntervalInSeconds(3600 * 6).build()
        remoteConfig?.setConfigSettingsAsync(configSettings)
        remoteConfig?.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    private fun setAlarms() {
        if (located) {
            Keeper(this, location!!)
            times = PTUtils.getTimes(this, location!!)
            Alarms(this, times!!)
        }
        else {
            Toast.makeText(
                this, getString(R.string.give_location_permission_toast),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun dailyUpdate() {
        val intent = Intent(this, DailyUpdateReceiver::class.java)
        intent.action = "daily"
        sendBroadcast(intent)
    }

    private fun getTodayScreenContent(): Array<String> {
        val hijri = UmmalquraCalendar()
        val hDayName = resources.getStringArray(R.array.week_days)[hijri[Calendar.DAY_OF_WEEK] - 1]

        val millisInDay = 1000 * 60 * 60 * 24
        hijri.timeInMillis = hijri.timeInMillis + pref.getInt("date_offset", 0) * millisInDay

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
        val receiver = ComponentName(this, DeviceBootReceiver::class.java)
        applicationContext.packageManager.setComponentEnabledSetting(
            receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

    @Composable
    fun MainUI() {
        val navController = rememberNavController()

        MyScaffold(
            title = stringResource(id = R.string.app_name),
            onBackPressed = { onBackPressedDispatcher.onBackPressed() },
            topBar = {
                TopAppBar(
                    backgroundColor = AppTheme.colors.primary,
                    elevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
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
                            MyText(text = stringResource(id = R.string.app_name))

                            Column(
                                Modifier
                                    .fillMaxHeight()
                                    .clickable {
                                        DateEditorDialog(
                                            object : DialogCallback {
                                                override fun refresh() {
                                                    getTodayScreenContent()
                                                }
                                            }
                                        ).show(
                                            this@MainActivity.supportFragmentManager,
                                            "DateEditorDialog"
                                        )
                                    },
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxHeight()
                                        .padding(horizontal = 10.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    val content = getTodayScreenContent()
                                    MyText(
                                        text = content[0],
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    MyText(
                                        text = content[1],
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = { MyBottomNavigation(navController = navController) },
            fab = {
                if (currentScreen.value == "Quran")
                    MyFloatingActionButton(
                        iconId = R.drawable.ic_quran_search,
                        description = stringResource(id = R.string.search_in_quran)
                    ) {
                        val intent = Intent(this, QuranSearcherActivity::class.java)
                        startActivity(intent)
                    }
            }
        ) {
            NavigationGraph(navController = navController, it)
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
                homeScreen = HomeScreen(this@MainActivity, pref, located, location)
                homeScreen!!.HomeUI()

                prayersScreen = null
                quranScreen = null
                athkarScreen = null
                moreScreen = null
            }
            composable(BottomNavItem.Prayers.screen_route) {
                prayersScreen = PrayersScreen(
                    this@MainActivity, pref, located, location, supportFragmentManager
                )
                prayersScreen!!.PrayersUI()

                homeScreen = null
                quranScreen = null
                athkarScreen = null
                moreScreen = null
            }
            composable(BottomNavItem.Quran.screen_route) {
                quranScreen = QuranScreen(
                    this@MainActivity, pref, supportFragmentManager
                )
                quranScreen!!.QuranUI()

                homeScreen = null
                prayersScreen = null
                athkarScreen = null
                moreScreen = null
            }
            composable(BottomNavItem.Athkar.screen_route) {
                athkarScreen = AthkarScreen(this@MainActivity)
                athkarScreen!!.AthkarUI()

                homeScreen = null
                prayersScreen = null
                quranScreen = null
                moreScreen = null
            }
            composable(BottomNavItem.More.screen_route) {
                moreScreen = MoreScreen(this@MainActivity)
                moreScreen!!.OtherUI()

                homeScreen = null
                prayersScreen = null
                quranScreen = null
                athkarScreen = null
            }
        }
    }

}