package bassamalim.hidaya.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import bassamalim.hidaya.core.Globals
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.services.PrayersNotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DeviceBootReceiver : BroadcastReceiver() {

    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Globals.TAG, "in device boot receiver")

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val intent1 = Intent(context, DailyUpdateReceiver::class.java)
            intent1.action = "boot"
            context.sendBroadcast(intent1)

            scope.launch {
                val prayersNotificationEnabled =
                    prayersRepository.getContinuousPrayersNotificationEnabled().first()
                if (prayersNotificationEnabled) {
                    val serviceIntent = Intent(context, PrayersNotificationService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        context.startForegroundService(serviceIntent)
                    else
                        context.startService(serviceIntent)
                }
            }
        }
    }

}