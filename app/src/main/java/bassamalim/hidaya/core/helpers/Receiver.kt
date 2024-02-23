package bassamalim.hidaya.core.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build

class ReceiverManager(
    private val ctx: Context,
    private val receiver: BroadcastReceiver,
    private val intentFilter: IntentFilter
) {

    fun register() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ctx.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        else
            ctx.registerReceiver(receiver, intentFilter)
    }

    fun unregister() {
        try {
            ctx.unregisterReceiver(receiver)
        } catch (_: IllegalArgumentException) {}
    }

}