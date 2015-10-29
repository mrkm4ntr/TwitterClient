package mrkm4ntr.twitterclient.views;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import mrkm4ntr.twitterclient.R;
import mrkm4ntr.twitterclient.data.TwitterContract;

public class TimelineFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = TimelineFragment.class.getSimpleName();
    private StatusAdapter mStatusAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    private static final int TIMELINE_LOADER = 0;

    private static final String[] STATUS_COLUMNS = {
            TwitterContract.StatusEntry._ID,
            TwitterContract.StatusEntry.COLUMN_TEXT,
            TwitterContract.StatusEntry.COLUMN_CREATE_AT
    };

    public TimelineFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mStatusAdapter = new StatusAdapter(getActivity(), null, 0);
        View rootView =  inflater.inflate(R.layout.fragment_timeline, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listView_timeline);
        mListView.setAdapter(mStatusAdapter);
        // TODO add itemClickListener
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TIMELINE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOder = TwitterContract.StatusEntry.COLUMN_CREATE_AT + " DESC";
        return new CursorLoader(getActivity(),
                TwitterContract.StatusEntry.CONTENT_URI,
                STATUS_COLUMNS, null, null, sortOder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mStatusAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mStatusAdapter.swapCursor(null);
    }

}
