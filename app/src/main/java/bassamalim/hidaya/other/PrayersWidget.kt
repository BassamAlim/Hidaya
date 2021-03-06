package bassamalim.hidaya.other

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import bassamalim.hidaya.R
import bassamalim.hidaya.helpers.Keeper

class PrayersWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int
        ) {
            Utils.onActivityCreateSetLocale(context)
            
            val timesList = Keeper(context).retrieveStrTimes()

            if (timesList != null) {
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
        }
    }

}