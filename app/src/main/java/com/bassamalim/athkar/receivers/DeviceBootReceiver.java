package com.bassamalim.athkar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bassamalim.athkar.Constants;

public class DeviceBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Constants.TAG, "in device boot receiver");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Intent intent1 = new Intent(context, DailyUpdateReceiver.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }

}
