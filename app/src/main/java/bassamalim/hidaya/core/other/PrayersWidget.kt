package bassamalim.hidaya.core.other

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import bassamalim.hidaya.R
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.core.utils.PTUtils

class PrayersWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val timesList = getTimesList(context) ?: return

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.widget_prayers)

        views.setTextViewText(R.id.widget_fajr, timesList[0])
        views.setTextViewText(R.id.widget_duhr, timesList[1])
        views.setTextViewText(R.id.widget_asr, timesList[2])
        views.setTextViewText(R.id.widget_maghrib, timesList[3])
        views.setTextViewText(R.id.widget_ishaa, timesList[4])

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getTimesList(context: Context): Array<String?>? {
        val times = PTUtils.getStrTimes(context) ?: return null

        val prayerNames = context.resources.getStringArray(R.array.prayer_names)

        val result = arrayOfNulls<String>(times.size-1)

        var j = 0
        for (i in 0..4) {
            if (i == 1) j++  // To skip sunrise

            result[i] = "${prayerNames[j]}\n${translateNums(context, times[j], true)}"

            j++
        }

        return result
    }

}