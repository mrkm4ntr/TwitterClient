package mrkm4ntr.twitterclient.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import mrkm4ntr.twitterclient.R;
import mrkm4ntr.twitterclient.data.TwitterContract;
import mrkm4ntr.twitterclient.sync.TwitterSyncAdapter;
import mrkm4ntr.twitterclient.views.ProgressDialogFragment;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class TweetActivity extends AppCompatActivity {

    private static final String LOG_TAG = TweetActivity.class.getSimpleName();

    private View mLayout;
    private Button mTweetButton;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLayout = findViewById(R.id.layout_post_tweet);

        mEditText = (EditText) findViewById(R.id.editText);
        mTweetButton = (Button) findViewById(R.id.button_tweet);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    mTweetButton.setEnabled(false);
                } else {
                    mTweetButton.setEnabled(true);
                }
            }
        });

        mTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = mEditText.getText().toString();
                new TweetTask(status, getApplicationContext()).execute();
            }
        });

        if (mEditText.getText().length() == 0) {
            mTweetButton.setEnabled(false);
        }
    }

    class TweetTask extends AsyncTask<Void, Void, Status> {

        private String mStatus;
        private Context mContext;
        private DialogFragment mDialogFragment;

        public TweetTask(String status, Context context) {
            mStatus = status;
            mContext = context;
        }

        @Override
        protected twitter4j.Status doInBackground(Void... voids) {
            try {Thread.sleep(3000);}catch(Exception e){}
            try {
                AccountManager accountManager = AccountManager.get(mContext);
                Account[] accounts = accountManager.getAccountsByType(
                        mContext.getString(R.string.sync_account_type));
                if (accounts.length > 0) {
                    Account account = accounts[0];
                    Twitter twitter = TwitterSyncAdapter.TWITTER;
                    String token = accountManager.blockingGetAuthToken(account,
                            mContext.getString(R.string.sync_account_type), true);
                    String tokenSecret = accountManager.getPassword(account);
                    twitter.setOAuthAccessToken(new AccessToken(token, tokenSecret));
                    return twitter.updateStatus(mStatus);
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mDialogFragment = ProgressDialogFragment.newInstance(
                    getString(R.string.message_progress_tweet));
            mDialogFragment.show(TweetActivity.this.getFragmentManager(), "dialog");
        }

        @Override
        protected void onPostExecute(twitter4j.Status status) {
            if (status != null) {
                User user = status.getUser();
                ContentValues contentValues = new ContentValues();
                contentValues.put(TwitterContract.StatusEntry._ID, status.getId());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_TEXT, status.getText());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_CREATE_AT,
                        status.getCreatedAt().getTime());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_NAME, user.getName());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL,
                        user.getProfileImageURL());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME,
                        user.getScreenName());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_LOCATION,
                        user.getLocation());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_BIO,
                        user.getDescription());
                getContentResolver().insert(TwitterContract.StatusEntry.CONTENT_URI, contentValues);
                finish();
            } else {
                if (mDialogFragment.getShowsDialog()) {
                    Dialog dialog = mDialogFragment.getDialog();
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                Snackbar.make(mLayout, R.string.message_error_post_tweet,
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

}
