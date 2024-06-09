package bassamalim.hidaya.core.receivers

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.helpers.Alarms
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.other.PrayersWidget
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.DBUtils
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.core.utils.PTUtils
import bassamalim.hidaya.core.utils.PrefUtils
import com.google.android.gms.location.LocationServices
import java.util.Calendar
import java.util.Random

class DailyUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Global.TAG, "in DailyUpdateReceiver")

        val sp = PrefUtils.getPreferences(context)
        val db = DBUtils.getDB(context)

        ActivityUtils.bootstrapApp(
            context = context,
            sp = sp,
            db = db,
            isFirstLaunch = true
        )

        val now = Calendar.getInstance()
        if ((intent.action == "daily" && notUpdatedToday(sp, now)) || intent.action == "boot") {
            when (LocationType.valueOf(PrefUtils.getString(sp, Prefs.LocationType))) {
                LocationType.Auto -> locate(context, sp, db, now)
                LocationType.Manual -> {
                    val cityId = PrefUtils.getInt(sp, Prefs.CityID)
                    if (cityId == -1) return
                    val city = db.cityDao().getCity(cityId)

                    val location = Location("")
                    location.latitude = city.latitude
                    location.longitude = city.longitude

                    update(context, sp, db, location, now)
                }
                LocationType.None -> return
            }

            pickWerd(sp)
        }
        else Log.i(Global.TAG, "dead intent in daily update receiver")

        setTomorrow(context)
    }

    private fun notUpdatedToday(
        sp: SharedPreferences,
        now: Calendar
    ): Boolean {
        return PrefUtils.getInt(sp, Prefs.LastDailyUpdateDay) != now[Calendar.DATE]
    }

    private fun locate(
        context: Context,
        sp: SharedPreferences,
        db: AppDatabase,
        now: Calendar
    ) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation.addOnSuccessListener {
                    location: Location? -> update(context, sp, db, location, now)
            }
        }
    }

    private fun update(
        context: Context,
        sp: SharedPreferences,
        db: AppDatabase,
        location: Location?,
        now: Calendar
    ) {
        if (location == null) {
            val storedLoc = LocUtils.retrieveLocation(sp)
            if (storedLoc == null) {
                Log.e(Global.TAG, "no available location in DailyUpdate")
                return
            }
        }
        else LocUtils.storeLocation(sp, location)

        val times = PTUtils.getTimes(sp, db) ?: return

        Alarms(context, times)

        updateWidget(context, times)

        updated(sp, now)
    }

    private fun updateWidget(
        context: Context,
        times: Array<Calendar?>
    ) {
        val intent = Intent(context, PrayersWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra("times", times)

        val ids = AppWidgetManager.getInstance(context.applicationContext).getAppWidgetIds(
            ComponentName(context.applicationContext, PrayersWidget::class.java)
        )

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        context.sendBroadcast(intent)
    }

    private fun updated(sp: SharedPreferences, now: Calendar) {
        val str = "Last Daily Update: ${now[Calendar.YEAR]}/${now[Calendar.MONTH] + 1}" +
            "/${now[Calendar.DATE]}" + " ${now[Calendar.HOUR_OF_DAY]}:${now[Calendar.MINUTE]}"

        sp.edit()
            .putInt(Prefs.LastDailyUpdateDay.key, now[Calendar.DATE])
            .putString(Prefs.DailyUpdateRecord.key, str)
            .apply()
    }

    private fun pickWerd(sp: SharedPreferences) {
        sp.edit()
            .putInt(Prefs.WerdPage.key, Random().nextInt(Global.QURAN_PAGES))
            .putBoolean(Prefs.WerdDone.key, false)
            .apply()
    }

    private fun setTomorrow(context: Context) {
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

        val alarm = context.applicationContext
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendIntent)
    }

}