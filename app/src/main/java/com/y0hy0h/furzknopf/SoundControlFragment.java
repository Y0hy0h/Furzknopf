package com.y0hy0h.furzknopf;

import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class SoundControlFragment extends Fragment {
    // tag for use in Log-statements
    private static final String LOG_TAG = SoundControlFragment.class.getSimpleName();

    private static SoundController mSoundController;
    private static SoundController.BigFartListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain fragment when runtime change occurs in Activity.
        setRetainInstance(true);

        mSoundController = new SoundController(mListener, getActivity().getAssets());
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
        if (mSoundController.getRegularSoundsLoaded() == 0) {
            mSoundController.initAndLoadSounds(getActivity().getAssets());
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Free resources when not restarting, if API level is appropriate.
        if (Build.VERSION.SDK_INT >= 11 && !getActivity().isChangingConfigurations()) {
            mSoundController.freeResources();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSoundController.freeResources();
    }

    public SoundController getSoundController()
    {
        return mSoundController;
    }
}
