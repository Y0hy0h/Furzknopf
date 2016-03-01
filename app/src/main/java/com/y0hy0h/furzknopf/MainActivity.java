package com.y0hy0h.furzknopf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // tag for use in Log-statements
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Vibrator mVibrator;

    private ImageButton mFartbutton;

    // toast objects to prevent multiple toasts from stacking
    private static Toast mToastNoSoundLoaded;

    private static SoundControlFragment mSoundControl;
    private static final String FRAGMENT_TAG = "soundControlFragment";

    // cooldown after which big fart is played
    private static int mCoolDown;
    // String for onSaveInstance's Bundle key
    private static final String STATE_COOLDOWN = "cooldown";

    // flag, if bigFart is currently playing
    private static boolean mBigFartPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind SoundControlFragment.
        FragmentManager fm = getSupportFragmentManager();
        mSoundControl = (SoundControlFragment) fm.findFragmentByTag(FRAGMENT_TAG);

        if (mSoundControl == null) {
            mSoundControl = new SoundControlFragment();
            fm.beginTransaction().add(mSoundControl, FRAGMENT_TAG).commit();
        }

        // Bind onTouchListener to fartbutton.
        // This allows the button to fart when pressed down.
        mFartbutton = (ImageButton) findViewById(R.id.fartbutton);
        mFartbutton.setDrawingCacheEnabled(true);
        mFartbutton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (onOpaquePixel(view, (int) event.getX(), (int) event.getY())) {
                            mFartbutton.setPressed(true);
                            playFart();
                            return true;
                        } else {
                            return false;
                        }
                    }

                    case MotionEvent.ACTION_MOVE: {
                        if (onOpaquePixel(view, (int) event.getX(), (int) event.getY())) {
                            return false;
                        }
                        // else fall thru
                    }

                    case MotionEvent.ACTION_UP : {
                        if (!mBigFartPlaying) {
                            mFartbutton.postDelayed(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            mFartbutton.setPressed(false);
                                        }
                                    },
                                    25
                            );
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
                return false;
            }
        });

        // Initialize cooldown. Survives onStop().
        if (savedInstanceState != null) {
            mCoolDown = savedInstanceState.getInt(STATE_COOLDOWN);
        } else {
            resetCoolDown();
        }
    }

    private boolean onOpaquePixel(View view, int x, int y)
    {
        // Check for touch on opaque part of button
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());
        int color = 0;
        try {
            color = bmp.getPixel(x, y);
        } catch (IllegalArgumentException e)
        {
            Log.d(LOG_TAG, "Touch outside image bounds.");
        }

        return color != Color.TRANSPARENT;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Load vibrator.
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Stop vibration, if just recreating app.
        if (Build.VERSION.SDK_INT >= 11 && !isChangingConfigurations()) {
            mVibrator.cancel();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_COOLDOWN, mCoolDown);

        super.onSaveInstanceState(outState);
    }

    /**
     * Plays a fart when touched.
     * This is the method called through the fartbutton's onTouchListener.
     */
    private void playFart() {
        // Abort if big fart is currently playing.
        if (mBigFartPlaying) {
            return;
        }

        // Play regular or big fart depending on cooldown.
        if (mCoolDown > 0) {
            mCoolDown--;
            regularFart();
        } else {
            resetCoolDown();
            bigFart();
        }
    }

    /**
     * Plays big fart, vibrates and keeps button pressed.
     * @see MainActivity#bigFart()
     */
    private void regularFart() {
        if (!mSoundControl.getSoundController().playRegularFart()) {
            reportNoSoundLoaded();
        }
    }

    /**
     * Plays big fart, vibrates and keeps button pressed.
     * @see MainActivity#regularFart()
     * @see SoundController#playBigFart()
     */
    private void bigFart() {
        if (!mSoundControl.getSoundController().bigFartLoaded()) {
            reportNoSoundLoaded();
            return;
        }

        mBigFartPlaying = true;
        long duration = mSoundControl.getSoundController().playBigFart(mVibrator);

        mFartbutton.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        mBigFartPlaying = false;
                        mFartbutton.setPressed(false);
                    }
                },
                duration
        );
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

    /**
     * Resets the cooldown.
     */
    private void resetCoolDown() {
        mCoolDown = getNewCoolDown();
    }

    /**
     * Cooldown is at least 75, maximum is 150 with increasing probability.
     * @return A new value for the cooldown (respecting its bounds).
     */
    public static int getNewCoolDown() {
        return 150 - Utility.getMappedRandomInt(75, 2);
    }
}