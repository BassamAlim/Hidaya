package bassamalim.hidaya.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.ID
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.other.Utils
import bassamalim.hidaya.receivers.NotificationReceiver
import java.util.*

class Alarms {

    private val context: Context
    private var pref: SharedPreferences

    constructor(gContext: Context, gTimes: Array<Calendar?>) {
        context = gContext
        pref = PreferenceManager.getDefaultSharedPreferences(context)

        setAll(gTimes)
    }

    constructor(gContext: Context, id: ID) {
        context = gContext
        pref = PreferenceManager.getDefaultSharedPreferences(context)

        if (id.ordinal in 0..5)
            setPrayerAlarm(id, Keeper(context).retrieveTimes()[id.ordinal])
        else if (id.ordinal in 6..9)
            setExtraAlarm(id)
    }

    /**
     * Finds out if the desired function and executes it
     */
    private fun setAll(times: Array<Calendar?>) {
        setPrayerAlarms(times)
        setExtraAlarms()
    }

    private fun setPrayerAlarms(times: Array<Calendar?>) {
        Log.i(Global.TAG, "in set prayer alarms")
        for (i in 0..5) {
            val mappedId: ID = Utils.mapID(i)!!
            if (pref.getInt(mappedId.toString() + "notification_type", 2) != 0)
                setPrayerAlarm(mappedId, times[i])
        }
    }

    /**
     * Set an alarm for the given prayer time
     *
     * @param id the ID of the prayer
     */
    private fun setPrayerAlarm(id: ID, time: Calendar?) {
        Log.i(Global.TAG, "in set alarm for: $id")

        // adjust the time with the delay
        val adjustment: Long = pref.getLong(id.toString() + "time_adjustment", 0)
        val adjusted = time!!.timeInMillis + adjustment

        if (System.currentTimeMillis() <= adjusted) {
            val intent = Intent(context, NotificationReceiver::class.java)
            if (id == ID.SHOROUQ) intent.action = "extra"
            else intent.action = "prayer"
            intent.putExtra("id", id.ordinal)
            intent.putExtra("time", adjusted)

            val myAlarm: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                context, id.ordinal, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, adjusted, pendingIntent)

            Log.i(Global.TAG, "alarm $id set")
        }
        else Log.i(Global.TAG, "$id Passed")
    }

    /**
     * Set the extra alarms based on the user preferences
     */
    private fun setExtraAlarms() {
        Log.i(Global.TAG, "in set extra alarms")

        val today = Calendar.getInstance()

        if (pref.getBoolean(context.getString(R.string.morning_athkar_key), true)) setExtraAlarm(ID.MORNING)
        if (pref.getBoolean(context.getString(R.string.evening_athkar_key), true)) setExtraAlarm(ID.EVENING)
        if (pref.getBoolean(context.getString(R.string.daily_werd_key), true)) setExtraAlarm(ID.DAILY_WERD)
        if (pref.getBoolean(context.getString(R.string.friday_kahf_key), true)
            && today[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
            setExtraAlarm(ID.FRIDAY_KAHF)
    }

    /**
     * Set an alarm for a specific time
     *
     * @param id The ID of the alarm.
     */
    private fun setExtraAlarm(id: ID?) {
        Log.i(Global.TAG, "in set extra alarm")

        var defaultH = 0
        val defaultM = 0
        when (id) {
            ID.MORNING -> defaultH = 5
            ID.EVENING -> defaultH = 16
            ID.DAILY_WERD -> defaultH = 21
            ID.FRIDAY_KAHF -> defaultH = 13
            else -> {}
        }

        val hour: Int = pref.getInt(id.toString() + "hour", defaultH)
        val minute: Int = pref.getInt(id.toString() + "minute", defaultM)

        val time = Calendar.getInstance()
        time[Calendar.HOUR_OF_DAY] = hour
        time[Calendar.MINUTE] = minute
        time[Calendar.SECOND] = 0
        time[Calendar.MILLISECOND] = 0

        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = "extra"
        intent.putExtra("id", id!!.ordinal)
        intent.putExtra("time", time.timeInMillis)

        val myAlarm: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context, id.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        myAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)

        Log.i(Global.TAG, "alarm $id set")
    }

}