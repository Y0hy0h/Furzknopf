package com.y0hy0h.furzknopf.widget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.y0hy0h.furzknopf.Utility;

import java.util.Locale;

public class VolatileFartService extends Service {

    private static final String LOG_TAG = VolatileFartService.class.getSimpleName();

    private static final String ACTION_PLAY_FART = "com.y0hy0h.furzknopf.action.FOO";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLAY_FART.equals(action)) {
                Log.v(LOG_TAG, "Intent received");

                String pathToFile = String.format(
                        Locale.US,
                        "fart%02d.ogg",
                        Utility.getIntBetween(1, 15)
                );
               MediaPlayer tempMediaPlayer = MediaPlayer.create(
                        this,
                        Uri.parse("file:///android_asset/"+pathToFile)
                );
                tempMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        stopSelf();
                    }
                });
                Log.v(LOG_TAG, "Start playing");
                tempMediaPlayer.start();
            }
        }

        return START_NOT_STICKY;
    }

    public static Intent createIntentPlayFart(Context context) {
        Intent intent = new Intent(context, VolatileFartService.class);
        intent.setAction(ACTION_PLAY_FART);

        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
