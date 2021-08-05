package com.bassamalim.athkar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bassamalim.athkar.services.DailyUpdateService;

public class DailyUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, DailyUpdateService.class);
        context.startService(intent1);
    }

}
