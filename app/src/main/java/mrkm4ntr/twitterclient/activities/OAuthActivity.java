package mrkm4ntr.twitterclient.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import mrkm4ntr.twitterclient.R;
import mrkm4ntr.twitterclient.sync.TwitterSyncAdapter;
import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class OAuthActivity extends AppCompatActivity {

    private static final String LOG_TAG = OAuthActivity.class.getSimpleName();

    private RequestToken mRequestToken;
    private View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mView = findViewById(R.id.oauth_layout);

        final EditText pinText = (EditText) findViewById(R.id.password_pin);
        Button authButton = (Button) findViewById(R.id.button_auth);

        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pin = pinText.getText().toString();
                new GetAccessTokenTask(pin).execute();
            }
        });

        Button openTwitterButton = (Button) findViewById(R.id.button_open_twitter);
        openTwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetRequestTokenTask().execute();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class GetRequestTokenTask extends AsyncTask<Void, Void, RequestToken> {

        @Override
        protected RequestToken doInBackground(Void... params) {
            try {
                Twitter twitter = TwitterSyncAdapter.TWITTER;
                twitter.setOAuthAccessToken(null);
                return twitter.getOAuthRequestToken();
            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(RequestToken requestToken) {
            super.onPostExecute(requestToken);
            if (requestToken != null) {
                mRequestToken = requestToken;
                startActivity(new Intent(
                        Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthorizationURL())));
            } else {
                Snackbar.make(mView,
                        getApplicationContext().getString(R.string.message_error_requestToken),
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    public class GetAccessTokenTask extends AsyncTask<Void, Void, AccessToken> {

        private static final int SYNC_INTERVAL = 60 * 60;
        private static final int FLEX_TIME = SYNC_INTERVAL / 3;

        private final String mPin;

        public GetAccessTokenTask(String pin) {
            mPin = pin;
        }

        @Override
        protected AccessToken doInBackground(Void... params) {
            try {
                return TwitterSyncAdapter.TWITTER.getOAuthAccessToken(mRequestToken, mPin);
            } catch (Exception e) {
                Log.d(LOG_TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(AccessToken accessToken) {
            super.onPostExecute(accessToken);
            if (accessToken != null) {
                TwitterSyncAdapter.TWITTER.setOAuthAccessToken(accessToken);
                Context context = getApplicationContext();
                String type = context.getString(R.string.sync_account_type);
                Account account = new Account(context.getString(R.string.app_name), type);
                AccountManager accountManager = AccountManager.get(getApplication());
                accountManager.setPassword(account, accessToken.getTokenSecret());
                accountManager.setAuthToken(account, type, accessToken.getToken());
                SyncRequest request = new SyncRequest.Builder()
                        .syncPeriodic(SYNC_INTERVAL, FLEX_TIME)
                        .setSyncAdapter(account, getString(R.string.content_authority))
                        .setExtras(new Bundle()).build();
                ContentResolver.requestSync(request);
                setResult(RESULT_OK);
                finish();
            } else {
                Snackbar.make(mView,
                        getApplicationContext().getString(R.string.message_error_auth),
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

}
