package bassamalim.hidaya.core.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build

class ReceiverWrapper(
    private val context: Context,
    private val intentFilter: IntentFilter,
    private val receiver: BroadcastReceiver
) {

    fun register() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                context.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
            else
                context.registerReceiver(receiver, intentFilter)
        } catch (_: IllegalArgumentException) {}
    }

    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: IllegalArgumentException) {}
    }

}