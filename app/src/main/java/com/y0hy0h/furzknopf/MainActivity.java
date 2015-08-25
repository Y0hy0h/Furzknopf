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

public class MainActivity extends AppCompatActivity {

    // tag for use in Log-statements
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static SoundPool mSoundPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialize SoundPool depending on API version and load sounds.
        int maxStreams = 6;
        if (Build.VERSION.SDK_INT >= 21)
            createSoundPoolWithBuilder(maxStreams);
        else
            createSoundPoolWithConstructor(maxStreams);

        loadSounds();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity was stopped, release SoundPool's resources.
        mSoundPool.release();
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
     * Loads the default fart sounds into the SoundPool.
     */
    private void loadSounds() {
        //TODO: Load all sounds.
        AssetManager assetManager = getAssets();

        try {
            // Load only one sound, for testing purposes.
            Log.v(LOG_TAG, Integer.toString(mSoundPool.load(assetManager.openFd("fart01.wav"), 1)));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Default sound could not be loaded.", e);
        }
    }

    /**
     * OnClick-action playing a fart.
     * @param view The view that was clicked.
     */
    public void playFart(View view) {
        //TODO: Randomize sounds and frequency/speed.
        mSoundPool.play(1, 1, 1, 0, 0, 1);
    }
}
