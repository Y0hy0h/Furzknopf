package com.y0hy0h.furzknopf;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ImageButton;

public class RetainedFragment extends Fragment {
    // tag for use in Log-statements
    private static final String LOG_TAG = RetainedFragment.class.getSimpleName();

    private static FartController mFartController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain fragment when runtime change occurs in Activity.
        setRetainInstance(true);

        final ImageButton fartbutton = (ImageButton) getActivity().findViewById(R.id.fartbutton);
        FartController.BigFartListener listener = new FartController.BigFartListener() {
                @Override
                public void bigFartStarted() {
                    fartbutton.setPressed(true);
                }

                @Override
                public void bigFartEnded() {
                    fartbutton.setPressed(false);
                }
        };

        setFartController(new FartController(listener, getActivity().getAssets()));
    }

    public void setFartController(FartController fartController) {
        mFartController = fartController;
    }

    public FartController getFartController()
    {
        return mFartController;
    }
}
