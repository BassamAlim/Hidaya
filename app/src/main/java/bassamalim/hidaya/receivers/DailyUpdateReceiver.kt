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
import androidx.preference.PreferenceManager
import bassamalim.hidaya.helpers.Alarms
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.PrayersWidget
import bassamalim.hidaya.utils.DBUtils
import bassamalim.hidaya.utils.PTUtils
import com.google.android.gms.location.LocationServices
import java.util.*

class DailyUpdateReceiver : BroadcastReceiver() {

    private lateinit var context: Context
    private lateinit var pref: SharedPreferences
    private var now = Calendar.getInstance()

    override fun onReceive(gContext: Context, intent: Intent) {
        Log.i(Global.TAG, "in DailyUpdateReceiver")

        context = gContext
        pref = PreferenceManager.getDefaultSharedPreferences(context)

        if ((intent.action == "daily" && needed()) || intent.action == "boot") {
            when (pref.getString("location_type", "auto")) {
                "auto" -> locate()
                "manual" -> {
                    val cityId = pref.getInt("city_id", -1)
                    if (cityId == -1) return
                    val city = DBUtils.getDB(context).cityDao().getCity(cityId)

                    val location = Location("")
                    location.latitude = city.latitude
                    location.longitude = city.longitude

                    update(location)
                }
                "none" -> return
            }
        }
        else Log.i(Global.TAG, "dead intent walking in daily update receiver")

        setTomorrow()
    }

    private fun needed(): Boolean {
        val lastDay = pref.getInt("last_day", 0)

        val time = Calendar.getInstance()
        time[Calendar.HOUR_OF_DAY] = Global.DAILY_UPDATE_HOUR
        time[Calendar.MINUTE] = Global.DAILY_UPDATE_MINUTE

        return lastDay != now[Calendar.DATE] && time.timeInMillis < now.timeInMillis
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
        var loc: Location? = location
        if (loc == null) {
            loc = Keeper(context).retrieveLocation()
            if (loc == null) {
                Log.e(Global.TAG, "No available location in DailyUpdate")
                return
            }
        }

        Keeper(context, loc)

        val times = PTUtils.getTimes(context, loc)

        Alarms(context, times)

        updateWidget()

        updated()
    }

    private fun updateWidget() {
        val intent = Intent(context, PrayersWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context.applicationContext)
            .getAppWidgetIds(ComponentName(context.applicationContext, PrayersWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

    private fun updated() {
        val str = "Last Daily Update: ${now[Calendar.YEAR]}/${now[Calendar.MONTH] + 1}" +
            "/${now[Calendar.DATE]}" + " ${now[Calendar.HOUR_OF_DAY]}:${now[Calendar.MINUTE]}"

        val editor = pref.edit()
        editor.putInt("last_day", now[Calendar.DATE])
        editor.putString("last_daily_update", str)
        editor.apply()
    }

    private fun setTomorrow() {
        val intent = Intent(context, DailyUpdateReceiver::class.java)
        intent.action = "daily"

        val time = Calendar.getInstance()
        time[Calendar.DATE]++
        time[Calendar.HOUR_OF_DAY] = Global.DAILY_UPDATE_HOUR
        time[Calendar.MINUTE] = Global.DAILY_UPDATE_MINUTE

        val pendIntent = PendingIntent.getBroadcast(
            context, 1210, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendIntent)
    }

}