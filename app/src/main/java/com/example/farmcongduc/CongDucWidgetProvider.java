package com.example.farmcongduc;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class CongDucWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_KNOCK_WIDGET = "com.example.farmcongduc.ACTION_KNOCK_WIDGET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int meritCount = prefs.getInt(MainActivity.PREF_MERIT_COUNT_KEY, 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.tv_widget_merit_count, "" + meritCount);

        Intent intent = new Intent(context, WidgetUpdateService.class);
        intent.setAction(ACTION_KNOCK_WIDGET);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // THAY ĐỔI: Đăng ký click cho toàn bộ widget container
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}