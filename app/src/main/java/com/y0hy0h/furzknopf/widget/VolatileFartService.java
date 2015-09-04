package com.y0hy0h.furzknopf.widget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class VolatileFartService extends Service {

    private static final String LOG_TAG = VolatileFartService.class.getSimpleName();

    private static final String ACTION_PLAY_FART = "com.y0hy0h.furzknopf.action.playFart";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLAY_FART.equals(action)) {
                Log.v(LOG_TAG, "Intent received");

                // Get the file to be played.
                String pathToFile = String.format(
                        Locale.US,
                        "fart%02d.wav",
                        4
                );

                // Load file and play.
                InputStream audioIS = null;

                try {
                    audioIS = getAssets().openFd(pathToFile).createInputStream();
                    playSound(audioIS);

                } catch (IOException e) {
                    Log.e(LOG_TAG, "File "+pathToFile+" could not be loaded.", e);
                } finally {
                    // Close InputStream.
                    if (audioIS != null) {
                        try {
                            audioIS.close();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Error closing InputStream", e);
                        }
                    }
                }
            }
        }

        return START_NOT_STICKY;
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

        // Start playback.
        at.play();
    }

    public static Intent createIntentPlayFart(Context context) {
        Intent intent = new Intent(context, VolatileFartService.class);
        intent.setAction(ACTION_PLAY_FART);

        return intent;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "Service being destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
