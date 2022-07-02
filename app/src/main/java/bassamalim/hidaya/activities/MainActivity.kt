package bassamalim.hidaya.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.databinding.ActivityMainBinding
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils
import bassamalim.hidaya.receivers.DailyUpdateReceiver
import bassamalim.hidaya.receivers.DeviceBootReceiver
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

    companion object {
        var location: Location? = null
        var times: Array<Calendar?>? = null
        var located = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theme = Utils.onActivityCreateSetTheme(this)
        language = Utils.onActivityCreateSetLocale(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTodayScreen()
        setContentView(binding.root)

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        setSupportActionBar(binding.topBar)
        setTitle(R.string.app_name)

        initNavBar()

        initFirebase()

        setAlarms()

        testDb()

        dailyUpdate()

        setupBootReceiver()
    }

    override fun onRestart() {
        super.onRestart()

        val newTheme: String = pref.getString(
            getString(R.string.theme_key),
            getString(R.string.default_theme)
        )!!
        val newLanguage: String = pref.getString(
            getString(R.string.language_key),
            getString(R.string.default_language)
        )!!

        if (newTheme != theme || newLanguage != language) {
            theme = newTheme
            language = newLanguage

            Utils.refresh(this)
        }
    }

    private fun initNavBar() {
        val navHostFragment: NavHostFragment = (supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment)
        val navController: NavController = navHostFragment.navController
        setupWithNavController(binding.navView, navController)
    }

    private fun initFirebase() {
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600 * 6).build()
        // update at most every six hours
        remoteConfig?.setConfigSettingsAsync(configSettings)
        remoteConfig?.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    private fun setAlarms() {
        val intent: Intent = intent
        located = intent.getBooleanExtra("located", false)
        if (located) {
            location = intent.getParcelableExtra("location")
            Keeper(this, location!!)
            times = Utils.getTimes(this, location)
            //times = test();
            Alarms(this, times!!)
        }
        else {
            Toast.makeText(
                this, getString(R.string.give_location_permission_toast),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun test(): Array<Calendar?> {
        val tester = arrayOfNulls<Calendar>(6)
        tester[0] = Calendar.getInstance()
        tester[0]?.set(Calendar.HOUR_OF_DAY, 0)
        tester[0]?.set(Calendar.MINUTE, 9)
        tester[0]?.set(Calendar.SECOND, 0)
        tester[1] = Calendar.getInstance()
        tester[1]?.set(Calendar.HOUR_OF_DAY, 13)
        tester[1]?.set(Calendar.MINUTE, 48)
        tester[2] = Calendar.getInstance()
        tester[2]?.set(Calendar.HOUR_OF_DAY, 0)
        tester[2]?.set(Calendar.MINUTE, 1)
        tester[3] = Calendar.getInstance()
        tester[3]?.set(Calendar.HOUR_OF_DAY, 0)
        tester[3]?.set(Calendar.MINUTE, 27)
        tester[4] = Calendar.getInstance()
        tester[4]?.set(Calendar.HOUR_OF_DAY, 0)
        tester[4]?.set(Calendar.MINUTE, 5)
        tester[5] = Calendar.getInstance()
        tester[5]?.set(Calendar.HOUR_OF_DAY, 2)
        tester[5]?.set(Calendar.MINUTE, 43)
        return tester
    }

    private fun testDb() {
        try {
            val db: AppDatabase = Room.databaseBuilder(
                this, AppDatabase::class.java, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db")
                .allowMainThreadQueries().build()
            db.suarDao().getFav() // if there is a problem in the db it will cause an error
        } catch (e: Exception) {
            Utils.reviveDb(this)
        }

        val lastVer: Int = pref.getInt("last_db_version", 1)
        if (Global.dbVer > lastVer) Utils.reviveDb(this)
    }

    private fun dailyUpdate() {
        val day: Int = pref.getInt("last_day", 0)

        val today = Calendar.getInstance()
        if (day != today[Calendar.DAY_OF_MONTH]) {
            val hour = 0

            val intent = Intent(this, DailyUpdateReceiver::class.java)
            intent.action = "daily"
            intent.putExtra("time", hour)

            val time = Calendar.getInstance()
            time[Calendar.HOUR_OF_DAY] = hour

            val pendIntent: PendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val myAlarm: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            myAlarm.setRepeating(
                AlarmManager.RTC_WAKEUP, time.timeInMillis, AlarmManager.INTERVAL_DAY, pendIntent
            )

            sendBroadcast(intent)
        }
    }

    private fun setTodayScreen() {
        val hijri = UmmalquraCalendar()
        val hYear = " " + hijri.get(Calendar.YEAR)
        val hMonth = " " + resources.getStringArray(R.array.hijri_months)[Calendar.MONTH]
        val hDay = "" + hijri.get(Calendar.DATE)
        var hijriStr: String = resources
            .getStringArray(R.array.week_days)[hijri.get(Calendar.DAY_OF_WEEK) - 1].toString() + " "
        hijriStr += Utils.translateNumbers(this, hDay) + hMonth +
                Utils.translateNumbers(this, hYear)
        binding.hijriView.text = hijriStr

        val gregorian = Calendar.getInstance()
        val mYear = " " + gregorian[Calendar.YEAR]
        val mMonth = " " + resources
            .getStringArray(R.array.gregorian_months)[gregorian[Calendar.MONTH]]
        val mDay = "" + gregorian[Calendar.DATE]
        val gregorianStr = (Utils.translateNumbers(this, mDay)
                + mMonth + Utils.translateNumbers(this, mYear))
        binding.gregorianView.text = gregorianStr
    }

    private fun setupBootReceiver() {
        val receiver = ComponentName(this, DeviceBootReceiver::class.java)
        val pm: PackageManager = applicationContext.packageManager

        pm.setComponentEnabledSetting(
            receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

}