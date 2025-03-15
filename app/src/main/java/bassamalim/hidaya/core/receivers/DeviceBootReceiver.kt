package bassamalim.hidaya.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import bassamalim.hidaya.core.Globals

class DeviceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Globals.TAG, "in device boot receiver")

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val intent1 = Intent(context, DailyUpdateReceiver::class.java)
            intent1.action = "boot"
            context.sendBroadcast(intent1)
        }
    }

}