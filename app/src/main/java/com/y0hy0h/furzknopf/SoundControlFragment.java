package com.y0hy0h.furzknopf;

import android.annotation.TargetApi;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;

public class SoundControlFragment extends Fragment {
    // tag for use in Log-statements
    private static final String LOG_TAG = SoundControlFragment.class.getSimpleName();

    private static SoundPool mSoundPool;
    // contains the IDs of the regular farts
    private static LinkedList<Integer> mLoadedSoundIDs;
    private static int mBigFartID = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain fragment when runtime change occurs in Activity.
        setRetainInstance(true);

        initAndLoadSounds();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set appropriate volume control.
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStart() {
        super.onStart();

        // If necessary initialize SoundPool and load sounds.
        if (mLoadedSoundIDs == null) {
            initAndLoadSounds();
        }
    }

    /**
     * Initializes the SoundPool and ID-queue and loads default sounds.
     */
    private void initAndLoadSounds() {
        // Initialize SoundPool depending on API version,
        mSoundPool = createSoundPoolCompatibly(6);
        mLoadedSoundIDs = new LinkedList<>();
        loadSounds();
    }

    /**
     * Initializes the SoundPool with the preferred method depending on the API level.
     * @param maxStreams The number of sounds that can be played back simultaneously.
     */
    @TargetApi(21)
    @SuppressWarnings("deprecation")
    static SoundPool createSoundPoolCompatibly(int maxStreams) {
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
     * Loads the default fart sounds into the SoundPool and enqueues the sound's IDs.
     */
    private void loadSounds() {

        final AssetManager assetManager = getActivity().getAssets();

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

    @Override
    public void onStop() {
        super.onStop();

        // Free resources when not restarting, if API level is appropriate.
        if (Build.VERSION.SDK_INT >= 11 && !getActivity().isChangingConfigurations()) {
            freeResources();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        freeResources();
    }

    /**
     * Frees most of the resources that are kept by the app.
     */
    private void freeResources() {
        // Activity was stopped, release SoundPool's and queue's resources.
        if (mLoadedSoundIDs != null) {
            mSoundPool.release();
            mLoadedSoundIDs.clear();
            mLoadedSoundIDs = null;
            mBigFartID = -1;
        }
    }

    int getRegularSoundsLoaded() {
        return mLoadedSoundIDs.size();
    }

    /**
     * Plays a regular fart and vibrates.
     * Assumes at least one file is loaded.
     *
     * @see SoundControlFragment#playBigFart()
     */
    public void playRegularFart() {

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
     * @return amount of milliseconds that the big fart will last
     * @see SoundControlFragment#playRegularFart()
     */
    long playBigFart() {
        // Choose random frequency.
        float freq = Utility.getFloatBetween(0.9f, 1.2f);

        // Play chosen sound with chosen frequency.
        mSoundPool.play(mBigFartID, 1, 1, 0, 0, freq);

        // Return whole duration.
        return (long) (3813 / freq);
    }

    /**
     * Returns whether the big fart is already loaded.
     * @return whether bigFart is already loaded.
     */
    boolean bigFartLoaded() {
        return mBigFartID != -1;
    }
}
