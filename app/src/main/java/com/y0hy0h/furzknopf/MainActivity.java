package com.y0hy0h.furzknopf;

import android.annotation.TargetApi;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // tag for use in Log-statements
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static SoundPool mSoundPool;
    private static LinkedList<Integer> mLoadedSoundIDs;
    private static Random mRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialize SoundPool depending on API version,
        // initialize queue and Random and load sounds.
        int maxStreams = 6;
        if (Build.VERSION.SDK_INT >= 21)
            createSoundPoolWithBuilder(maxStreams);
        else
            createSoundPoolWithConstructor(maxStreams);

        mLoadedSoundIDs = new LinkedList<>();
        mRandom = new Random();
        loadSounds();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity was stopped, release SoundPool's and queue's resources.
        mSoundPool.release();
        mLoadedSoundIDs.clear();
        mLoadedSoundIDs = null;
        mRandom = null;
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
     * Creates SoundPool the new way (API >=21)
     * @param maxStreams The maximal amount of simultaneous sounds to play.
     */
    @TargetApi(21)
    private void createSoundPoolWithBuilder(int maxStreams) {
        // Set up attributes.
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        // Create SoundPool.
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(maxStreams)
                .build();
    }

    /**
     * Creates SoundPool the old way (API <21), ensuring compatibility.
     * @param maxStreams The maximal amount of simultaneous sounds to play.
     */
    @SuppressWarnings("deprecation")
    private void createSoundPoolWithConstructor(int maxStreams) {
        // Create SoundPool and set VolumeControl.
        mSoundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    /**
     * Loads the default fart sounds into the SoundPool and enqueues the sound's IDs.
     */
    private void loadSounds() {

        AssetManager assetManager = getAssets();

        try {
            // Load all standard sounds and store IDs in queue.
            for (int i = 1; i <= 15; i++) {
                String pathToSound = String.format(Locale.US, "fart%02d.wav", i);
                Log.v(LOG_TAG, "Loading "+pathToSound);
                mLoadedSoundIDs.add(mSoundPool.load(assetManager.openFd(pathToSound), 1));
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Default sounds could not be loaded.", e);
        }
    }

    /**
     * OnClick-action playing a fart.
     * @param view The view that was clicked.
     */
    public void playFart(View view) {
        // Skip up to 5 files randomly.
        // Square the random number and map it between 1 and 5 for falloff in probability.
        float randomNumber = mRandom.nextFloat();
        randomNumber = randomNumber * randomNumber;
        int skipAmount = (int) (randomNumber * 5);

        // Skip to chosen soundID, move it to tail of list.
        int nextSoundID = mLoadedSoundIDs.remove(skipAmount);
        mLoadedSoundIDs.addLast(nextSoundID);

        // Choose random frequency.
        float freq = mRandom.nextFloat() * 0.75f + 0.75f;

        // Play chosen sound with chosen frequency.
        mSoundPool.play(nextSoundID, 1, 1, 0, 0, freq);
    }
}