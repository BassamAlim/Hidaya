package bassamalim.hidaya.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import bassamalim.hidaya.core.Globals
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.helpers.BootWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class DeviceBootReceiver : BroadcastReceiver() {

    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Globals.TAG, "in device boot receiver")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            dailyUpdate(context)

            startPrayersNotificationService(context)
        }
    }

    private fun dailyUpdate(context: Context) {
        val intent1 = Intent(context, DailyUpdateReceiver::class.java)
        intent1.action = "boot"
        context.sendBroadcast(intent1)
    }

    private fun startPrayersNotificationService(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<BootWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

}