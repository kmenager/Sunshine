package io.github.kmenager.sunshine.app.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import io.github.kmenager.sunshine.app.sync.SunshineSyncAdapter;


/**
 * Provider for a horizontally expandable widget showing today's weather.
 *
 * Delegates widget updating to {@link TodayWidgetIntentService} to ensure that
 * data retrieval is done on a background thread
 */
public class TodayWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, io.github.kmenager.sunshine.app.widget.TodayWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, io.github.kmenager.sunshine.app.widget.TodayWidgetIntentService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, io.github.kmenager.sunshine.app.widget.TodayWidgetIntentService.class));
        }
    }
}
