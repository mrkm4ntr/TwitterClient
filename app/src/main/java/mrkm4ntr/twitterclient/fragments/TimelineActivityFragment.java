package mrkm4ntr.twitterclient.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mrkm4ntr.twitterclient.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class TimelineActivityFragment extends Fragment {

    public TimelineActivityFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }
}
