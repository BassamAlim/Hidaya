package bassamalim.hidaya.core.receivers

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.helpers.Alarms
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.other.PrayersWidget
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.DBUtils
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.core.utils.PTUtils
import com.google.android.gms.location.LocationServices
import java.util.Calendar
import java.util.Random

class DailyUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Global.TAG, "in DailyUpdateReceiver")

        val preferencesDS = PreferencesDataSource(
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        val db = DBUtils.getDB(context)

        ActivityUtils.bootstrapApp(
            context = context,
            preferencesDS = preferencesDS,
            db = db,
            isFirstLaunch = true
        )

        val now = Calendar.getInstance()
        if ((intent.action == "daily" && notUpdatedToday(preferencesDS, now)) || intent.action == "boot") {
            when (LocationType.valueOf(preferencesDS.getString(Preference.LocationType))) {
                LocationType.AUTO -> locate(context, preferencesDS, db, now)
                LocationType.MANUAL -> {
                    val cityId = preferencesDS.getInt(Preference.CityID)
                    if (cityId == -1) return
                    val city = db.cityDao().getCity(cityId)

                    val location = Location("")
                    location.latitude = city.latitude
                    location.longitude = city.longitude

                    update(
                        context = context,
                        preferencesDS = preferencesDS,
                        db = db,
                        location = location,
                        now = now
                    )
                }
                LocationType.NONE -> return
            }

            pickWerd(preferencesDS)
        }
        else Log.i(Global.TAG, "dead intent in daily update receiver")

        setTomorrow(context)
    }

    private fun notUpdatedToday(
        preferencesDS: PreferencesDataSource,
        now: Calendar
    ): Boolean {
        val lastUpdate = Calendar.getInstance()
        lastUpdate.timeInMillis = preferencesDS.getLong(Preference.LastDailyUpdateMillis)
        return lastUpdate[Calendar.DATE] != now[Calendar.DATE]
    }

    private fun locate(
        context: Context,
        preferencesDS: PreferencesDataSource,
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
                    location: Location? -> update(context, preferencesDS, db, location, now)
            }
        }
    }

    private fun update(
        context: Context,
        preferencesDS: PreferencesDataSource,
        db: AppDatabase,
        location: Location?,
        now: Calendar
    ) {
        if (location == null) {
            val storedLoc = LocUtils.retrieveLocation(
                preferencesDS.getString(Preference.StoredLocation)
            )
            if (storedLoc == null) {
                Log.e(Global.TAG, "no available location in DailyUpdate")
                return
            }
        }
        else LocUtils.storeLocation(
            location = location,
            locationPreferenceSetter = { json ->
                preferencesDS.setString(Preference.StoredLocation, json)
            }
        )

        val times = PTUtils.getTimes(preferenceDS = preferencesDS, db = db) ?: return

        Alarms(gContext = context, gTimes = times)

        updateWidget(context = context, times = times)

        updated(preferencesDS = preferencesDS, now = now)
    }

    private fun updateWidget(
        context: Context,
        times: Array<Calendar?>
    ) {
        val intent = Intent(context, PrayersWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra("times", times)

        val ids = AppWidgetManager
            .getInstance(context.applicationContext)
            .getAppWidgetIds(
                ComponentName(context.applicationContext, PrayersWidget::class.java)
            )

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        context.sendBroadcast(intent)
    }

    private fun updated(
        preferencesDS: PreferencesDataSource,
        now: Calendar
    ) {
        preferencesDS.setLong(Preference.LastDailyUpdateMillis, now.timeInMillis)


    }

    private fun pickWerd(preferencesDS: PreferencesDataSource) {
        preferencesDS.setInt(Preference.WerdPage, Random().nextInt(Global.QURAN_PAGES))
        preferencesDS.setBoolean(Preference.WerdDone, false)
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