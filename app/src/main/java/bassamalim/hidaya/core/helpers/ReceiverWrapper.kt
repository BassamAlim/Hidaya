package bassamalim.hidaya.core.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.core.content.ContextCompat

class ReceiverWrapper(
    private val context: Context,
    private val intentFilter: IntentFilter,
    private val broadcastReceiver: BroadcastReceiver
) {

    fun register() {
        try {
            // Exported: the wrapped broadcasts (download completion) come from the system's
            // download provider, another app as far as Android 13+ is concerned, and a
            // non-exported receiver never hears them
            ContextCompat.registerReceiver(
                context,
                broadcastReceiver,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (_: IllegalArgumentException) {}
    }

    fun unregister() {
        try {
            context.unregisterReceiver(broadcastReceiver)
        } catch (_: IllegalArgumentException) {}
    }

}