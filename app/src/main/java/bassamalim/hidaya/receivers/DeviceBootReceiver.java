package bassamalim.hidaya.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import bassamalim.hidaya.other.Global;

public class DeviceBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Global.TAG, "in device boot receiver");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent intent1 = new Intent(context, DailyUpdateReceiver.class);
            intent1.setAction("boot");
            context.sendBroadcast(intent1);
        }
    }
}
