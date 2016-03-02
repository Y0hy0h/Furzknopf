package com.y0hy0h.furzknopf.widget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.y0hy0h.furzknopf.SoundController;

public class PermanentFartService extends Service {

    private static final String LOG_TAG = PermanentFartService.class.getSimpleName();

    private static final String ACTION_BASE = "com.y0hy0h.furzknopf.action";
    private static final String ACTION_PLAY_FART = ACTION_BASE + ".playFart";
    private static final String ACTION_POKE = ACTION_BASE + ".poke";

    private SoundController mSoundControl;

    /**
     * Plays a fart, if right Intent is passed.
     * @param intent The action the service is supposed to do.
     * @param flags No flags supported by this service.
     * @param startId No start ID supported by this service.
     * @return START_NOT_STICKY.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Start");

        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY_FART: {
                    Log.d(LOG_TAG, "fart");
                    if (mSoundControl == null) {
                        initSoundControl();
                    }

                    try {
                        mSoundControl.playRegularFart();
                    } catch (SoundController.NoSoundLoadedException e) {
                        initSoundControl();
                    }
                    break;
                }

                case ACTION_POKE: {
                    Log.d(LOG_TAG, "Poke");
                    initSoundControl();
                    break;
                }
            }

        }

        return START_NOT_STICKY;
    }

    private void initSoundControl() {
        mSoundControl = new SoundController(getAssets());
    }

    /**
     * Helper method that provides an Intent to play a fart.
     * @param context The context that is passed on to the Intent.
     * @return An Intent to play a fart.
     */
    public static Intent createIntentPlayFart(Context context) {
        Intent intent = new Intent(context, PermanentFartService.class);
        intent.setAction(ACTION_PLAY_FART);

        return intent;
    }

    public static Intent createIntentPoke(Context context) {
        Intent intent = new Intent(context, PermanentFartService.class);
        intent.setAction(ACTION_POKE);

        return intent;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "Service being destroyed");
    }

    /**
     * This service does not provide binding.
     * Therefore this method does nothing.
     * @param intent Not supported.
     * @return Not supported.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
