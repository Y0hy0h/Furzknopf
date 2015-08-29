package com.y0hy0h.furzknopf;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class FartbuttonWidget extends AppWidgetProvider {

    private static final String LOG_TAG = FartbuttonWidget.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(),
                R.layout.fartbutton_widget
        );
        Intent playIntent = VolatileFartService.createIntentPlayFart(context);
        PendingIntent playPendingIntent = PendingIntent.getService(context, 0, playIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.fartbutton_widget, playPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}

