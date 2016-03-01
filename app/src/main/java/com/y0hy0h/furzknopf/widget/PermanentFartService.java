package com.y0hy0h.furzknopf.widget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.y0hy0h.furzknopf.MainActivity;
import com.y0hy0h.furzknopf.R;
import com.y0hy0h.furzknopf.SoundController;
import com.y0hy0h.furzknopf.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Locale;

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

        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY_FART: {
                    if (mSoundControl == null || !mSoundControl.playRegularFart()) {
                        mSoundControl = new SoundController();
                        mSoundControl.initAndLoadSounds(getAssets());

                        mSoundControl.playRegularFart();
                    }
                    break;
                }

                case ACTION_POKE: {
                    mSoundControl = new SoundController();
                    mSoundControl.initAndLoadSounds(getAssets());
                    break;
                }
            }

        }

        return START_NOT_STICKY;
    }

    private String loadNextSoundFile() {

        // Read remaining cooldown and played sounds' list.
        SharedPreferences sharedPrefs = getSharedPreferences(
                getString(R.string.fartPref_key),
                Context.MODE_PRIVATE
        );

        int cooldown = sharedPrefs.getInt(
                getString(R.string.fartPref_cooldown),
                MainActivity.getNewCoolDown()
        );

        String rawPlayedList = sharedPrefs.getString(
                getString(R.string.fartPref_playedList),
                "01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,"
        );
        String[] stringPlayedList = rawPlayedList.split(",");
        LinkedList<Integer> playedList = new LinkedList<>();
        // note that the last comma does not create an extra entry
        for (String stringNext : stringPlayedList) {
            playedList.add( Integer.parseInt(stringNext) );
        }
        // Choose sound and move it to tail.
        int skipAmount = Utility.getMappedRandomInt(5, 2);
        int nextSoundNumber = playedList.remove(skipAmount);
        playedList.add(nextSoundNumber);

        // Save new values in SharedPreferences.
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(
                getString(R.string.fartPref_cooldown),
                cooldown
        );
        String newPlayedList = "";
        for (int i = 0; i < playedList.size(); i++) {
            newPlayedList += String.format(
                    Locale.US,
                    "%02d,",
                    playedList.get(i)
            );
        }
        editor.putString(
                getString(R.string.fartPref_playedList),
                newPlayedList
        );
        editor.commit();

        // Return chosen sound.
        return String.format(
                Locale.US,
                "fart%02d.wav",
                nextSoundNumber
        );
    }

    /**
     * Plays the sound that is represented by the given InputStream.
     * Uses AudioTrack and only works on mono 16 bit PCM .wav files.
     * @param audioIS The InputStream of the sound to be played.
     * @throws IOException
     */
    private void playSound(InputStream audioIS) throws IOException {

        // Get .wav header info and generate appropriate AudioTrack.
        WavInfo info = WavDecoder.readHeader(audioIS);
        AudioTrack at = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                info.mRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                info.mDataSize,
                AudioTrack.MODE_STATIC
        );

        // Write the InputStream's data into the AudioTrack.
        at.write(WavDecoder.readWavPcm(info, audioIS), 0, info.mDataSize);

        // Set up listener for sound completely playing.
        at.setNotificationMarkerPosition(info.mDataSize / 4);
        at.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {
                // Release the resources after playing.
                Log.v(LOG_TAG, "Finished playing");
                track.release();

                // Sound has been played, stop service.
                stopSelf();
            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {
            }
        });

        // Randomize frequency.
        at.setPlaybackRate((int) (info.mRate * Utility.getFloatBetween(0.75f, 1.5f)));
        Log.v(LOG_TAG, "Starting playback");
        // Start playback.
        at.play();
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
