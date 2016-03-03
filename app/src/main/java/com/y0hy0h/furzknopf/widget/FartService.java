package com.y0hy0h.furzknopf.widget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.y0hy0h.furzknopf.FartController;
import com.y0hy0h.furzknopf.R;

import java.util.Calendar;

public class FartService extends Service {

    private static final String LOG_TAG = FartService.class.getSimpleName();

    private static final String ACTION_BASE = "com.y0hy0h.furzknopf.action";
    private static final String ACTION_PLAY_REGULAR_FART = ACTION_BASE + ".playRegularFart";
    private static final String ACTION_PLAY_FART = ACTION_BASE + ".playFart";

    private final IBinder mFartBinder = new FartBinder();

    private FartController mSoundControl;
    private Handler mHandler;
    private Calendar mLastTimeUsed;
    private static final long TIMEOUT = 30 * 1000;

    // toast objects to prevent multiple toasts from stacking
    private static Toast mToastNoSoundLoaded;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "Creating");

        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mLastTimeUsed.getTimeInMillis() <
                                    Calendar.getInstance().getTimeInMillis() - TIMEOUT) {

                                stopSelf();
                            } else {
                                mHandler.postDelayed(this, TIMEOUT);
                            }
                        }
                    },
                    TIMEOUT + 100
        );

        mLastTimeUsed = Calendar.getInstance();
    }

    /**
     * Plays a fart, if right Intent is passed.
     * @param intent The action the service is supposed to do.
     * @param flags No flags supported by this service.
     * @param startId No start ID supported by this service.
     * @return START_NOT_STICKY.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY_REGULAR_FART: {
                    Log.d(LOG_TAG, "Farting");
                    if (mSoundControl == null) {
                        initSoundControl(true);
                    } else {
                        try {
                            mSoundControl.playFart((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
                            mLastTimeUsed = Calendar.getInstance();
                        } catch (FartController.NoSoundLoadedException e) {
                            reportNoSoundLoaded();
                        }
                    }
                    break;
                }
            }

        }

        return START_NOT_STICKY;
    }

    private void initSoundControl(boolean fartImmediately) {
        mSoundControl = new FartController(getAssets(), fartImmediately);
    }

    /**
     * Helper method that provides an Intent to play a fart.
     * @param context The context that is passed on to the Intent.
     * @return An Intent to play a fart.
     */
    public static Intent createIntentPlayRegularFart(Context context) {
        Intent intent = new Intent(context, FartService.class);
        intent.setAction(ACTION_PLAY_REGULAR_FART);

        return intent;
    }

    /**
     * Helper method that provides an Intent to play a fart.
     * @param context The context that is passed on to the Intent.
     * @return An Intent to play a fart.
     */
    public static Intent createIntentPlayFart(Context context) {
        Intent intent = new Intent(context, FartService.class);
        intent.setAction(ACTION_PLAY_FART);

        return intent;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Destroying service");
    }

    /**
     * Shows a toast reporting that the sound is not yet loaded.
     * Cancels toast, if already present, to prevent toast stacking.
     */
    private void reportNoSoundLoaded() {
        if (mToastNoSoundLoaded != null) {
            mToastNoSoundLoaded.cancel();
        }

        mToastNoSoundLoaded = Toast.makeText(this, R.string.noSoundLoaded, Toast.LENGTH_SHORT);
        mToastNoSoundLoaded.show();
    }

    public class FartBinder extends Binder {
        public FartController getFartController() {
            return FartService.this.mSoundControl;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "Unbinding");
        return super.onUnbind(intent);
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
        initSoundControl(false);
        return mFartBinder;
    }
}
