package bassamalim.hidaya.core.other

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.PrayerTimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.SortedMap
import javax.inject.Inject

@AndroidEntryPoint
class PrayersWidget : AppWidgetProvider() {

    @Inject private lateinit var prayersRepository: PrayersRepository
    @Inject private lateinit var notificationsRepository: NotificationsRepository
    @Inject private lateinit var locationRepository: LocationRepository
    @Inject private lateinit var appSettingsRepository: AppSettingsRepository

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        ActivityUtils.bootstrapApp(
            context = context,
            isFirstLaunch = true
        )
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        GlobalScope.launch {
            val prayerTimeStringMap = getPrayerTimeStringMap(context) ?: return@launch

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_prayers)

            views.setTextViewText(R.id.widget_fajr, prayerTimeStringMap[PID.FAJR])
            views.setTextViewText(R.id.widget_dhuhr, prayerTimeStringMap[PID.DHUHR])
            views.setTextViewText(R.id.widget_asr, prayerTimeStringMap[PID.ASR])
            views.setTextViewText(R.id.widget_maghrib, prayerTimeStringMap[PID.MAGHRIB])
            views.setTextViewText(R.id.widget_ishaa, prayerTimeStringMap[PID.ISHAA])

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private suspend fun getPrayerTimeStringMap(context: Context): SortedMap<PID, String>? {
        val location = locationRepository.getLocation().first() ?: return null

        val prayerTimes = PrayerTimeUtils.getPrayerTimesMap(
            settings = prayersRepository.getPrayerTimesCalculatorSettings().first(),
            timeOffsets = prayersRepository.getTimeOffsets().first(),
            timeZoneId = locationRepository.getTimeZone(location.cityId),
            location = location,
            calendar = Calendar.getInstance()
        )
        val prayerTimeStrings = PrayerTimeUtils.formatPrayerTimes(
            prayerTimeMap = prayerTimes,
            language = appSettingsRepository.getLanguage().first(),
            numeralsLanguage = appSettingsRepository.getNumeralsLanguage().first(),
            timeFormat = appSettingsRepository.getTimeFormat().first()
        )

        val prayerNames = context.resources.getStringArray(R.array.prayer_names)

        val strings = sortedMapOf<PID, String>()
        for (n in prayerNames.indices) {
            if (n == 1) continue  // To skip sunrise

            val name = prayerNames[n]
            val pid = prayerTimeStrings.keys.elementAt(n)
            val timeString = prayerTimeStrings[pid]

            strings[pid] = "$name\n$timeString"
        }
        return strings
    }

}