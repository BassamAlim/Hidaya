package com.bassamalim.athkar.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.bassamalim.athkar.helpers.Keeper;
import com.bassamalim.athkar.R;

public class PrayersWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String[] timesList = new Keeper(context).retrieveStrTimes();

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.prayers_widget);
        views.setTextViewText(R.id.widget_fajr, timesList[0]);
        views.setTextViewText(R.id.widget_duhr, timesList[1]);
        views.setTextViewText(R.id.widget_asr, timesList[2]);
        views.setTextViewText(R.id.widget_maghrib, timesList[3]);
        views.setTextViewText(R.id.widget_ishaa, timesList[4]);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}