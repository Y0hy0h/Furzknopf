package com.y0hy0h.furzknopf;

import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;

public class SoundController {
    // tag for use in Log-statements
    private static final String LOG_TAG = SoundController.class.getSimpleName();

    private static SoundPool mSoundPool;
    // contains the IDs of the regular farts
    private static LinkedList<Integer> mLoadedSoundIDs;
    private static int mBigFartID = -1;
    private boolean mBigFartPlaying;
    private BigFartListener mBigFartListener;
    private int mCooldown;

    public SoundController(final AssetManager assetManager) {
        initAndLoadSounds(assetManager);
    }

    public SoundController(BigFartListener bigFartListener, final AssetManager assetManager) {
        mBigFartListener = bigFartListener;

        initAndLoadSounds(assetManager);
    }

    /**
     * Initializes the SoundPool with the preferred method depending on the API level.
     * @param maxStreams The number of sounds that can be played back simultaneously.
     */
    @SuppressWarnings("deprecation")
    public static SoundPool createSoundPoolCompatibly(int maxStreams) {
        SoundPool resSoundPool;

        if (Build.VERSION.SDK_INT >= 21) {
            // Set up attributes.
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            // Create SoundPool.
            resSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(maxStreams)
                    .build();
        } else {
            // Create SoundPool.
            resSoundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }

        return resSoundPool;
    }

    /**
     * Cooldown is at least 75, maximum is 150 with increasing probability.
     * @return A new value for the cooldown (respecting its bounds).
     */
    public static int getNewCoolDown() {
        return 150 - Utility.getMappedRandomInt(75, 2);
    }

    public void setBigFartListener(BigFartListener bigFartListener) {
        mBigFartListener = bigFartListener;
    }

    /**
     * Initializes the SoundPool and ID-queue and loads default sounds.
     */
    public void initAndLoadSounds(final AssetManager assetManager) {
        // Initialize SoundPool depending on API version,
        mSoundPool = createSoundPoolCompatibly(6);
        mLoadedSoundIDs = new LinkedList<>();
        loadSounds(assetManager);

        mCooldown = getNewCoolDown();
    }

    /**
     * Loads the default fart sounds into the SoundPool and enqueues the sound's IDs.
     */
    public void loadSounds(final AssetManager assetManager) {

        new Thread(new Runnable() {
            private int tempBigFartID;

            @Override
            public void run() {
                mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        if (sampleId == tempBigFartID) {
                            mBigFartID = sampleId;
                        } else {
                            mLoadedSoundIDs.add(sampleId);
                        }
                    }
                });

                try {
                    // Load all standard sounds and store IDs in queue.
                    for (int i = 1; i <= 15; i++) {
                        String pathToSound = String.format(Locale.US, "fart%02d.wav", i);
                        mSoundPool.load(assetManager.openFd(pathToSound), 1);
                    }

                    tempBigFartID = mSoundPool.load(assetManager.openFd("fart_big.wav"), 1);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Default sounds could not be loaded.", e);
                }
            }
        }).start();
    }

    /**
     * Frees most of the resources that are kept by the SoundController.
     */
    public void freeResources() {
        if (mLoadedSoundIDs != null) {
            mSoundPool.release();
            mLoadedSoundIDs.clear();
            mLoadedSoundIDs = null;
            mBigFartID = -1;
        }
    }

    int getRegularSoundsLoaded() {
        if (mLoadedSoundIDs != null) {
            return mLoadedSoundIDs.size();
        } else {
            return 0;
        }
    }

    /**
     * Plays a fart when touched.
     * This is the method called through the fartbutton's onTouchListener.
     */
    public boolean playFart(Vibrator vibrator) throws NoSoundLoadedException {
        // Abort if big fart is currently playing.
        if (mBigFartPlaying) {
            return false;
        }

        // Play regular or big fart depending on cooldown.
        if (mCooldown > 0) {
            mCooldown--;
            playRegularFart();
        } else {
            mCooldown = getNewCoolDown();
            playBigFart(vibrator);
        }

        return true;
    }

    /**
     * Plays a regular fart and vibrates.
     */
    public void playRegularFart() throws NoSoundLoadedException {

        if (getRegularSoundsLoaded() == 0)
        {
            throw new NoSoundLoadedException();
        }

        int skipAmount = 0;

        // Skip up to 5 files randomly, if possible.
        if (mLoadedSoundIDs.size() >= 5) { // at least 5 sounds loaded
            skipAmount = Utility.getMappedRandomInt(5, 2);
        }

        // Skip to chosen soundID, move it to tail of list.
        int nextSoundID = mLoadedSoundIDs.remove(skipAmount);
        mLoadedSoundIDs.addLast(nextSoundID);

        // Choose random frequency.
        float freq = Utility.getFloatBetween(0.75f, 1.5f);

        // Play chosen sound with chosen frequency.
        mSoundPool.play(nextSoundID, 1, 1, 0, 0, freq);
    }

    /**
     * Plays a big fart and vibrates.
     *
     * @param vibrator The vibrator to use.
     * @return amount of milliseconds that the big fart will last
     */
    long playBigFart(Vibrator vibrator) {
        // Choose random frequency.
        float freq = Utility.getFloatBetween(0.9f, 1.2f);

        mBigFartListener.bigFartStarted();

        // Play chosen sound with chosen frequency.
        mSoundPool.play(mBigFartID, 1, 1, 0, 0, freq);

        long duration = (long) (3813 / freq);

        // Vibrate, add audio attributes depending on API level.
        if (Build.VERSION.SDK_INT >= 21) {
            vibrator.vibrate(
                    duration,
                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()
            );
        } else {
            vibrator.vibrate(duration);
        }

        mBigFartPlaying = true;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mBigFartPlaying = false;
                                    mBigFartListener.bigFartEnded();
                                }
                            },
                duration
        );

        return duration;
    }

    /**
     * Plays a big fart without vibrating.
     *
     * @return amount of milliseconds that the big fart will last
     */
    long playBigFart() {
        // Choose random frequency.
        float freq = Utility.getFloatBetween(0.9f, 1.2f);

        // Play chosen sound with chosen frequency.
        mSoundPool.play(mBigFartID, 1, 1, 0, 0, freq);

        return (long) (3813 / freq);
    }

    public boolean isBigFartPlaying() {
        return mBigFartPlaying;
    }

    /**
     * Returns whether the big fart is already loaded.
     * @return whether bigFart is already loaded.
     */
    boolean bigFartLoaded() {
        return mBigFartID != -1;
    }

    public interface BigFartListener {
        void bigFartStarted();
        void bigFartEnded();
    }

    public class NoSoundLoadedException extends Exception {
    }
}
