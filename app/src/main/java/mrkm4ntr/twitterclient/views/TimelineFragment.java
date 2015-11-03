package mrkm4ntr.twitterclient.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import mrkm4ntr.twitterclient.R;
import mrkm4ntr.twitterclient.data.TwitterContract;
import mrkm4ntr.twitterclient.sync.TwitterSyncAdapter;

public class TimelineFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    public static final String LOG_TAG = TimelineFragment.class.getSimpleName();
    private StatusAdapter mStatusAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    private static final int TIMELINE_LOADER = 0;

    private static final String[] STATUS_COLUMNS = {
            TwitterContract.StatusEntry._ID,
            TwitterContract.StatusEntry.COLUMN_TEXT,
            TwitterContract.StatusEntry.COLUMN_CREATE_AT,
            TwitterContract.StatusEntry.COLUMN_USER_NAME,
            TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL
    };

    private BroadcastReceiver mSyncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

    private BroadcastReceiver mTimeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mStatusAdapter.notifyDataSetChanged();
        }
    };

    public interface Callback {
        public void onItemSelected(Uri uri);
    }

    public TimelineFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mStatusAdapter = new StatusAdapter(getActivity(), null, 0);
        View rootView =  inflater.inflate(R.layout.fragment_timeline, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refreshLayout_timeline);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mListView = (ListView) rootView.findViewById(R.id.listView_timeline);
        mListView.setAdapter(mStatusAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity()).onItemSelected(
                            TwitterContract.StatusEntry.buildStatusUri(cursor.getLong(
                                    cursor.getColumnIndex(TwitterContract.StatusEntry._ID))));
                }
                mPosition = position;
            }
        });


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TIMELINE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = getActivity();
        context.registerReceiver(mSyncFinishedReceiver,
                new IntentFilter(TwitterSyncAdapter.SYNC_FINISHED));
        context.registerReceiver(mTimeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        Context context = getActivity();
        context.unregisterReceiver(mSyncFinishedReceiver);
        context.unregisterReceiver(mTimeTickReceiver);
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
            mListView.setItemChecked(mPosition, true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mStatusAdapter.swapCursor(null);
    }

    @Override
    public void onRefresh() {
        TwitterSyncAdapter.syncImmediately(getActivity());
    }
}
