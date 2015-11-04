package mrkm4ntr.twitterclient.views;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mrkm4ntr.twitterclient.R;
import mrkm4ntr.twitterclient.data.TwitterContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String DETAIL_URI = "URI";

    private static final String STATUS_SHARE_HASHTAG = "#TwitterClient";

    private ShareActionProvider mShareActionProvider;
    private String mStatus;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            TwitterContract.StatusEntry._ID,
            TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL,
            TwitterContract.StatusEntry.COLUMN_USER_NAME,
            TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME,
            TwitterContract.StatusEntry.COLUMN_TEXT,
            TwitterContract.StatusEntry.COLUMN_USER_LOCATION,
            TwitterContract.StatusEntry.COLUMN_USER_BIO
    };

    private ImageView mIconView;
    private TextView mNameView;
    private TextView mScreenNameView;
    private TextView mLocationView;
    private TextView mBioView;
    private TextView mTextView;

    public DetailFragment() {
        setHasOptionsMenu(true);
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
        mLocationView = (TextView) rootView.findViewById(R.id.detail_userLocation_textView);
        mBioView = (TextView) rootView.findViewById(R.id.detail_userBio_textView);
        mTextView = (TextView) rootView.findViewById(R.id.detail_text_textView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.isVisible()) {
            inflater.inflate(R.menu.menu_detail_fragment, menu);
            MenuItem menuItem = menu.findItem(R.id.action_share);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if (mStatus != null) {
                mShareActionProvider.setShareIntent(createShareStatusIntent());
            }
        }
    }

    private Intent createShareStatusIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mStatus + STATUS_SHARE_HASHTAG);
        return intent;
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
            String name = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_NAME));
            mNameView.setText(name);
            String screenName = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME));
            mScreenNameView.setText(screenName);
            String location = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_LOCATION));
            mLocationView.setText(location);
            String bio = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_BIO));
            mBioView.setText(bio);
            String text = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_TEXT));
            mTextView.setText(text);
            mStatus = text;

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareStatusIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
