package bassamalim.hidaya.activities

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.databinding.ActivityMainBinding
import bassamalim.hidaya.dialogs.DateEditorDialog
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.other.DialogCallback
import bassamalim.hidaya.receivers.DailyUpdateReceiver
import bassamalim.hidaya.receivers.DeviceBootReceiver
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.LangUtils
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.PrefUtils
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var remoteConfig: FirebaseRemoteConfig? = FirebaseRemoteConfig.getInstance()
    private lateinit var pref: SharedPreferences
    private lateinit var theme: String
    private lateinit var language: String
    private var times: Array<Calendar?>? = null

    companion object {
        var location: Location? = null
        var located = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        theme = ActivityUtils.onActivityCreateSetTheme(this)
        language = ActivityUtils.onActivityCreateSetLocale(this)

        binding = ActivityMainBinding.inflate(layoutInflater)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        setupTodayScreen()
        setContentView(binding.root)

        setSupportActionBar(binding.topBar)
        setTitle(R.string.app_name)

        initNavBar()

        initFirebase()

        setupListeners()

        setAlarms()

        dailyUpdate()

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

        setupTodayScreen()
    }

    private fun initNavBar() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        setupWithNavController(binding.navView, navController)
    }

    private fun initFirebase() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                                    // update at most every six hours
            .setMinimumFetchIntervalInSeconds(3600 * 6).build()
        remoteConfig?.setConfigSettingsAsync(configSettings)
        remoteConfig?.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    private fun setupListeners() {
        binding.dateSpace.setOnClickListener {
            DateEditorDialog(
                object : DialogCallback {
                    override fun refresh() {
                        setupTodayScreen()
                    }
                }
            ).show(this.supportFragmentManager, "DateEditorDialog")
        }
    }

    private fun setAlarms() {
        val intent = intent
        located = intent.getBooleanExtra("located", false)
        if (located) {
            location = intent.getParcelableExtra("location")

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

    private fun setupTodayScreen() {
        val hijri = UmmalquraCalendar()
        val hDayName = resources.getStringArray(R.array.week_days)[hijri[Calendar.DAY_OF_WEEK] - 1]

        val millisInDay = 1000 * 60 * 60 * 24
        hijri.timeInMillis = hijri.timeInMillis + pref.getInt("date_offset", 0) * millisInDay

        val hMonth = resources.getStringArray(R.array.hijri_months)[hijri[Calendar.MONTH]]
        val hijriStr = "$hDayName ${hijri[Calendar.DATE]} $hMonth ${hijri[Calendar.YEAR]}"
        binding.hijriView.text = LangUtils.translateNums(this, hijriStr, false)


        val gregorian = Calendar.getInstance()
        val mMonth = resources.getStringArray(R.array.gregorian_months)[gregorian[Calendar.MONTH]]
        val gregorianStr = "${gregorian[Calendar.DATE]} $mMonth ${gregorian[Calendar.YEAR]}"
        binding.gregorianView.text =
            LangUtils.translateNums(this, gregorianStr, false)
    }

    private fun setupBootReceiver() {
        val receiver = ComponentName(this, DeviceBootReceiver::class.java)
        applicationContext.packageManager.setComponentEnabledSetting(
            receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

}