package com.y0hy0h.furzknopf;

import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class RetainedFragment extends Fragment {
    // tag for use in Log-statements
    private static final String LOG_TAG = RetainedFragment.class.getSimpleName();

    private static FartController mFartController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain fragment when runtime change occurs in Activity.
        setRetainInstance(true);
    }

    public void setFartController(FartController fartController) {
        mFartController = fartController;
    }

    public FartController getFartController()
    {
        return mFartController;
    }
}
