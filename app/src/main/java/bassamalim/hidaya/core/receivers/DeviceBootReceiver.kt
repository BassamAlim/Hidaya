package bassamalim.hidaya.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import bassamalim.hidaya.core.Globals
import bassamalim.hidaya.core.services.PrayerReminderService

class DeviceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Globals.TAG, "in device boot receiver")

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val intent1 = Intent(context, DailyUpdateReceiver::class.java)
            intent1.action = "boot"
            context.sendBroadcast(intent1)

            val serviceIntent = Intent(context, PrayerReminderService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            }
            else {
                context.startService(serviceIntent)
            }
        }
    }

}