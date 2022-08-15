package bassamalim.hidaya.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import bassamalim.hidaya.other.Global

class DeviceBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Global.TAG, "in device boot receiver")

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val intent1 = Intent(context, DailyUpdateReceiver::class.java)
            intent1.action = "boot"
            context.sendBroadcast(intent1)
        }
    }

}