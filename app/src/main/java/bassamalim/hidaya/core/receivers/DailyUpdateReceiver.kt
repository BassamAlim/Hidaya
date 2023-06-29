package bassamalim.hidaya.core.receivers

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.helpers.Alarms
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.other.PrayersWidget
import bassamalim.hidaya.core.utils.*
import com.google.android.gms.location.LocationServices
import java.util.*

class DailyUpdateReceiver : BroadcastReceiver() {

    private lateinit var ctx: Context
    private lateinit var sp: SharedPreferences
    private var now = Calendar.getInstance()

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Global.TAG, "in DailyUpdateReceiver")

        ctx = context
        sp = PrefUtils.getPreferences(ctx)

        try {  // remove after a while
            ActivityUtils.onActivityCreateSetLocale(ctx)
            LocationType.valueOf(PrefUtils.getString(sp, Prefs.LocationType))
        } catch (e: Exception) {
            Log.e(Global.TAG, "Neuralyzing", e)
            ActivityUtils.clearAppData(ctx)
        }

        if ((intent.action == "daily" && notUpdatedToday()) || intent.action == "boot") {
            when (LocationType.valueOf(PrefUtils.getString(sp, Prefs.LocationType))) {
                LocationType.Auto -> locate()
                LocationType.Manual -> {
                    val cityId = PrefUtils.getInt(sp, Prefs.CityID)
                    if (cityId == -1) return
                    val city = DBUtils.getDB(ctx).cityDao().getCity(cityId)

                    val location = Location("")
                    location.latitude = city.latitude
                    location.longitude = city.longitude

                    update(location)
                }
                LocationType.None -> return
            }

            pickWerd()
        }
        else Log.i(Global.TAG, "dead intent in daily update receiver")

        setTomorrow()
    }

    private fun notUpdatedToday(): Boolean {
        return PrefUtils.getInt(sp, Prefs.LastDailyUpdateDay) != now[Calendar.DATE]
    }

    private fun locate() {
        if (ActivityCompat.checkSelfPermission(
                ctx, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                ctx, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(ctx)
                .lastLocation.addOnSuccessListener {
                    location: Location? -> update(location)
            }
        }
    }

    private fun update(location: Location?) {
        if (location == null) {
            val storedLoc = LocUtils.retrieveLocation(sp)
            if (storedLoc == null) {
                Log.e(Global.TAG, "no available location in DailyUpdate")
                return
            }
        }
        else LocUtils.storeLocation(sp, location)

        val times = PTUtils.getTimes(sp, DBUtils.getDB(ctx)) ?: return

        Alarms(ctx, times)

        updateWidget(times)

        updated()
    }

    private fun updateWidget(times: Array<Calendar?>) {
        val intent = Intent(ctx, PrayersWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra("times", times)

        val ids = AppWidgetManager.getInstance(ctx.applicationContext).getAppWidgetIds(
            ComponentName(ctx.applicationContext, PrayersWidget::class.java)
        )

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        ctx.sendBroadcast(intent)
    }

    private fun updated() {
        val str = "Last Daily Update: ${now[Calendar.YEAR]}/${now[Calendar.MONTH] + 1}" +
            "/${now[Calendar.DATE]}" + " ${now[Calendar.HOUR_OF_DAY]}:${now[Calendar.MINUTE]}"

        sp.edit()
            .putInt(Prefs.LastDailyUpdateDay.key, now[Calendar.DATE])
            .putString(Prefs.DailyUpdateRecord.key, str)
            .apply()
    }

    private fun pickWerd() {
        sp.edit()
            .putInt(Prefs.WerdPage.key, Random().nextInt(Global.QURAN_PAGES))
            .putBoolean(Prefs.WerdDone.key, false)
            .apply()
    }

    private fun setTomorrow() {
        val intent = Intent(ctx.applicationContext, DailyUpdateReceiver::class.java)
        intent.action = "daily"

        val time = Calendar.getInstance()
        time[Calendar.DATE]++
        time[Calendar.HOUR_OF_DAY] = Global.DAILY_UPDATE_HOUR
        time[Calendar.MINUTE] = Global.DAILY_UPDATE_MINUTE

        val pendIntent = PendingIntent.getBroadcast(
            ctx.applicationContext, 1210, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm =
            ctx.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendIntent)
    }

}