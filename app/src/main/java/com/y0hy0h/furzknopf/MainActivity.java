package com.y0hy0h.furzknopf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
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

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private ImageButton mFartbutton;

    // toast object to prevent multiple toasts from stacking
    private static Toast mToastNoSoundLoaded;

    private static RetainedFragment mRetainedFragment;
    private static final String FRAGMENT_TAG = "retainedFragment";

    private static FartController.BigFartListener mBigFartListener;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind RetainedFragment.
        FragmentManager fm = getSupportFragmentManager();
        mRetainedFragment = (RetainedFragment) fm.findFragmentByTag(FRAGMENT_TAG);

        if (mRetainedFragment == null) {
            mRetainedFragment = new RetainedFragment();
            fm.beginTransaction().add(mRetainedFragment, FRAGMENT_TAG).commit();

            FartController.BigFartListener listener = new FartController.BigFartListener() {
                @Override
                public void bigFartStarted() {
                    mFartbutton.setPressed(true);
                }

                @Override
                public void bigFartEnded() {
                    mFartbutton.setPressed(false);
                }
            };
            mRetainedFragment.setFartController(new FartController(listener, getAssets()));
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
                            try {
                                mRetainedFragment.getFartController().playFart(mVibrator);
                            } catch (FartController.NoSoundLoadedException e) {
                                reportNoSoundLoaded();
                            }
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

                    case MotionEvent.ACTION_UP: {
                        if (!mRetainedFragment.getFartController().isBigFartPlaying()) {
                            // delay up action to make it more visible
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

        // Set appropriate volume control.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    /**
     * Returns wheter the pixel at the given coordinates is opaque.
     *
     * @param view This view's drawing cache will be checked.
     * @param x The x coordinate to check.
     * @param y The y coordinate to check.
     * @return True iff the pixel at (x,y) of the view's drawing cache is transparent.
     */
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

        // Stop vibration, if not just recreating app.
        if (Build.VERSION.SDK_INT >= 11 && !isChangingConfigurations()) {
            mVibrator.cancel();
        }
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
}