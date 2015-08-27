package com.y0hy0h.furzknopf;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // tag for use in Log-statements
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Vibrator mVibrator;

    private ImageButton mFartbutton;

    // toast object to prevent multiple toasts from stacking
    private static Toast mToastNoSoundLoaded;

    private static SoundControlFragment mSoundControl;
    private static final String FRAGMENT_TAG = "soundControlFragment";

    // cooldown after which big fart is played
    private static int mCoolDown;
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
        mFartbutton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        playFart();
                        return false;
                    }

                    case MotionEvent.ACTION_UP : {
                        // if big fart is playing, consume up action
                        // to prevent button from popping back up
                        return mBigFartPlaying;
                    }
                }
                return false;
            }
        });

        // Initialize cooldown. Survives onStop().
        resetCoolDown();
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

        // Stop vibration.
        mVibrator.cancel();
    }

    /**
     * Plays a fart when touched.
     * This is the method called through the fartbutton's onTouchListener.
     */
    private void playFart() {
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
     * @see SoundControlFragment#playRegularFart()
     */
    private void regularFart() {
        if (mSoundControl.getRegularSoundsLoaded() > 0)
            mSoundControl.playRegularFart();
        else
            reportNoSoundLoaded();
    }

    /**
     * Plays big fart, vibrates and keeps button pressed.
     * @see MainActivity#regularFart()
     * @see SoundControlFragment#playBigFart()
     */
    private void bigFart() {
        if (!mSoundControl.bigFartLoaded()) {
            reportNoSoundLoaded();
            return;
        } else if (mBigFartPlaying) {
            return;
        }

        mBigFartPlaying = true;
        long duration = mSoundControl.playBigFart();

        // Vibrate, add audio attributes depending on API level.
        if (Build.VERSION.SDK_INT >= 21)
            mVibrator.vibrate(
                    duration,
                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()
            );
        else
            mVibrator.vibrate(duration);

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
     * Cooldown is at least 75, maximum is 150 with increasing probability.
     */
    private void resetCoolDown() {
        mCoolDown = 150 - Utility.getMappedRandomInt(75, 3);
    }
}