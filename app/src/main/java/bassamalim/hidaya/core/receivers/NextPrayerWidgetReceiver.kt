package bassamalim.hidaya.core.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.di.IoDispatcher
import bassamalim.hidaya.core.widgets.NextPrayerWidget
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NextPrayerWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var locationRepository: LocationRepository
    @Inject @IoDispatcher lateinit var dispatcher: CoroutineDispatcher

    override val glanceAppWidget: GlanceAppWidget
        get() = NextPrayerWidget(
            appSettingsRepository = appSettingsRepository,
            prayersRepository = prayersRepository,
            locationRepository = locationRepository,
            dispatcher = dispatcher
        )

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE) {
            MainScope().launch { glanceAppWidget.updateAll(context) }
            scheduleNextUpdate(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdates(context)
    }

    companion object {
        const val ACTION_UPDATE = "bassamalim.hidaya.NEXT_PRAYER_WIDGET_UPDATE"

        fun scheduleNextUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(
                AlarmManager.RTC,
                System.currentTimeMillis() + 60_000L,
                buildPendingIntent(context)
            )
        }

        fun cancelUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(buildPendingIntent(context))
        }

        private fun buildPendingIntent(context: Context) = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, NextPrayerWidgetReceiver::class.java).apply { action = ACTION_UPDATE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        private const val REQUEST_CODE = 5001
    }

}
