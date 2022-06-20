package bassamalim.hidaya.receivers

import android.Manifest
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
import bassamalim.hidaya.other.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

class DailyUpdateReceiver : BroadcastReceiver() {

    private var context: Context? = null
    private var pref: SharedPreferences? = null
    private var time = 0
    private var now: Calendar? = null

    override fun onReceive(gContext: Context, intent: Intent) {
        Log.i(Global.TAG, "in daily update receiver")
        context = gContext
        pref = PreferenceManager.getDefaultSharedPreferences(context!!)
        now = Calendar.getInstance()

        if (intent.action == "daily") {
            time = intent.getIntExtra("time", 0)

            if (needed()) locate()
            else Log.i(Global.TAG, "dead intent walking in daily update receiver")
        }
        else if (intent.action == "boot")
            locate()
    }

    private fun needed(): Boolean {
        val day: Int = pref!!.getInt("last_day", 0)

        val today = now!![Calendar.DATE]
        val hour = now!![Calendar.HOUR_OF_DAY]

        return day != today && time <= hour
    }

    private fun locate() {
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context!!)

        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener {
                    location: Location -> update(location)
            }
        }
    }

    private fun update(location: Location) {
        var loc: Location? = location
        if (loc == null) {
            loc = Keeper(context!!).retrieveLocation()
            if (loc == null) {
                Log.e(Global.TAG, "No available location in DailyUpdate")
                return
            }
        }

        Keeper(context!!, loc)

        val times = Utils.getTimes(context!!, loc)

        Alarms(context!!, times)

        updateWidget()

        updated()
    }

    private fun updateWidget() {
        val intent = Intent(context, PrayersWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(context!!.applicationContext)
            .getAppWidgetIds(ComponentName(context!!.applicationContext, PrayersWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context!!.sendBroadcast(intent)
    }

    private fun updated() {
        val editor: SharedPreferences.Editor = pref!!.edit()
        editor.putInt("last_day", now!![Calendar.DATE])
        editor.apply()
    }
}