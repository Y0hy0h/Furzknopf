package com.y0hy0h.furzknopf;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class FartbuttonWidget extends AppWidgetProvider {

    private static final String ACTION_PLAY_FART = "ActionPlayFart";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(),
                R.layout.fartbutton_widget
        );

        Intent playIntent = new Intent(context, FartbuttonWidget.class);
        playIntent.setAction(ACTION_PLAY_FART);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.fartbutton_widget, playPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String intentAction = intent.getAction();

        if (intentAction.equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
            final int appWidetID = intent.getExtras().getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
            );

            if (appWidetID != AppWidgetManager.INVALID_APPWIDGET_ID)
                this.onDeleted(context, new int[] {appWidetID});

        } else if (intentAction.equals(ACTION_PLAY_FART)) {

        }

        super.onReceive(context, intent);
    }
}

