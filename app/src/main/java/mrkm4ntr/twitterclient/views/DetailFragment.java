package mrkm4ntr.twitterclient.views;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mrkm4ntr.twitterclient.R;
import mrkm4ntr.twitterclient.data.TwitterContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String DETAIL_URI = "URI";

    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            TwitterContract.StatusEntry._ID,
            TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL,
            TwitterContract.StatusEntry.COLUMN_USER_NAME,
            TwitterContract.StatusEntry.COLUMN_TEXT
    };

    private ImageView mIconView;
    private TextView mNameView;
    private TextView mScreenNameView;
    private TextView mTextView;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DETAIL_URI);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mNameView = (TextView) rootView.findViewById(R.id.detail_name_textView);
        mScreenNameView = (TextView) rootView.findViewById(R.id.detail_screenName_textView);
        mTextView = (TextView) rootView.findViewById(R.id.detail_text_textView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(getActivity(), mUri, DETAIL_COLUMNS, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String profileImageUrl = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL));
            new StatusAdapter.UpdateImageViewTask(mIconView, profileImageUrl).execute();
            String name = data.getString(data.getColumnIndex(TwitterContract.StatusEntry.COLUMN_USER_NAME));
            mNameView.setText(name);
            String text = data.getString(data.getColumnIndex(TwitterContract.StatusEntry.COLUMN_TEXT));
            mTextView.setText(text);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
