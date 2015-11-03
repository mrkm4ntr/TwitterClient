package mrkm4ntr.twitterclient.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import mrkm4ntr.twitterclient.R;
import mrkm4ntr.twitterclient.data.TwitterContract;
import mrkm4ntr.twitterclient.sync.TwitterSyncAdapter;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class TweetActivity extends AppCompatActivity {

    private static final String LOG_TAG = TweetActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText editText = (EditText) findViewById(R.id.editText);
        Button tweetButton = (Button) findViewById(R.id.button_tweet);
        tweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = editText.getText().toString();
                new TweetTask(status, getApplicationContext()).execute();
            }
        });
    }

    class TweetTask extends AsyncTask<Void, Void, Status> {

        private String mStatus;
        private Context mContext;

        public TweetTask(String status, Context context) {
            mStatus = status;
            mContext = context;
        }

        @Override
        protected twitter4j.Status doInBackground(Void... voids) {
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
        protected void onPostExecute(twitter4j.Status status) {
            if (status != null) {
                User user = status.getUser();
                ContentValues contentValues = new ContentValues();
                contentValues.put(TwitterContract.StatusEntry._ID, status.getId());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_TEXT, status.getText());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_CREATE_AT, status.getCreatedAt().getTime());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_NAME, user.getName());
                contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL, user.getProfileImageURL());
                getContentResolver().insert(TwitterContract.StatusEntry.CONTENT_URI, contentValues);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT);
            }
        }
    }

}
