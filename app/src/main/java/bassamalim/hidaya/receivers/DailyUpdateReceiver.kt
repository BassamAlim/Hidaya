package bassamalim.hidaya.receivers

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.enums.LocationType
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.PrayersWidget
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.LocUtils
import bassamalim.hidaya.utils.PTUtils
import bassamalim.hidaya.utils.PrefUtils
import com.google.android.gms.location.LocationServices
import java.util.*

class DailyUpdateReceiver : BroadcastReceiver() {

    private lateinit var context: Context
    private lateinit var pref: SharedPreferences
    private var now = Calendar.getInstance()

    override fun onReceive(gContext: Context, intent: Intent) {
        Log.i(Global.TAG, "in DailyUpdateReceiver")

        context = gContext
        pref = PrefUtils.getPreferences(context)

        if ((intent.action == "daily" && needed()) || intent.action == "boot") {
            when (LocationType.valueOf(PrefUtils.getString(pref, Prefs.LocationType))) {
                LocationType.Auto -> locate()
                LocationType.Manual -> {
                    val cityId = PrefUtils.getInt(pref, Prefs.CityID)
                    if (cityId == -1) return
                    val city = DBUtils.getDB(context).cityDao().getCity(cityId)

                    val location = Location("")
                    location.latitude = city.latitude
                    location.longitude = city.longitude

                    update(location)
                }
                LocationType.None -> return
            }

            pickWerd()
        }
        else Log.i(Global.TAG, "dead intent walking in daily update receiver")

        setTomorrow()
    }

    private fun needed(): Boolean {
        return PrefUtils.getInt(pref, Prefs.LastDailyUpdateDay) != now[Calendar.DATE]
    }

    private fun locate() {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation.addOnSuccessListener {
                    location: Location? -> update(location)
            }
        }
    }

    private fun update(location: Location?) {
        if (location == null) {
            val storedLoc = LocUtils.retrieveLocation(pref)
            if (storedLoc == null) {
                Log.e(Global.TAG, "No available location in DailyUpdate")
                return
            }
        }
        else LocUtils.storeLocation(pref, location)

        val times = PTUtils.getTimes(pref, DBUtils.getDB(context))!!

        Alarms(context, times)

        updateWidget(times)

        updated()
    }

    private fun updateWidget(times: Array<Calendar?>) {
        val intent = Intent(context, PrayersWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra("times", times)

        val ids = AppWidgetManager.getInstance(context.applicationContext).getAppWidgetIds(
            ComponentName(context.applicationContext, PrayersWidget::class.java)
        )

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        context.sendBroadcast(intent)
    }

    private fun updated() {
        val str = "Last Daily Update: ${now[Calendar.YEAR]}/${now[Calendar.MONTH] + 1}" +
            "/${now[Calendar.DATE]}" + " ${now[Calendar.HOUR_OF_DAY]}:${now[Calendar.MINUTE]}"

        pref.edit()
            .putInt("last_day", now[Calendar.DATE])
            .putString("last_daily_update", str)
            .apply()
    }

    private fun pickWerd() {
        pref.edit()
            .putInt("today_werd_page", Random().nextInt(Global.QURAN_PAGES) - 1)
            .putBoolean("werd_done", false)
            .apply()
    }

    private fun setTomorrow() {
        val intent = Intent(context.applicationContext, DailyUpdateReceiver::class.java)
        intent.action = "daily"

        val time = Calendar.getInstance()
        time[Calendar.DATE]++
        time[Calendar.HOUR_OF_DAY] = Global.DAILY_UPDATE_HOUR
        time[Calendar.MINUTE] = Global.DAILY_UPDATE_MINUTE

        val pendIntent = PendingIntent.getBroadcast(
            context.applicationContext, 1210, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm =
            context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendIntent)
    }

}