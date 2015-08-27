package com.y0hy0h.furzknopf;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // tag for use in Log-statements
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private ImageButton mFartbutton;

    // toast object to prevent multiple toasts from stacking
    private static Toast mToastNoSoundLoaded;

    private static Random mRandom = new Random();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * OnTouch-action playing a fart.
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

    private void regularFart() {
        if (mSoundControl.getRegularSoundsLoaded() > 0)
            mSoundControl.playRegularFart();
        else
            reportNoSoundLoaded();
    }

    private void bigFart() {
        if (!mSoundControl.bigFartLoaded()) {
            reportNoSoundLoaded();
            return;
        } else if (mBigFartPlaying) {
            return;
        }

        mBigFartPlaying = true;
        long duration = mSoundControl.playBigFart();

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