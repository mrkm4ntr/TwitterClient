package mrkm4ntr.twitterclient.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import mrkm4ntr.twitterclient.sync.TwitterSyncAdapter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class OAuthActivity extends AppCompatActivity {

    private RequestToken mRequestToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText pinText = (EditText) findViewById(R.id.password_pin);
        Button authButton = (Button) findViewById(R.id.button_auth);

        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pin = pinText.getText().toString();
                new GetAccessTokenTask().execute(pin);
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

    public class GetRequestTokenTask extends AsyncTask<Void, Void, RequestToken> {

        @Override
        protected RequestToken doInBackground(Void... params) {
            try {
                return TwitterSyncAdapter.TWITTER.getOAuthRequestToken();
            } catch (TwitterException e) {
                Log.d("test", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(RequestToken requestToken) {
            super.onPostExecute(requestToken);
            mRequestToken = requestToken;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken
                    .getAuthorizationURL())));
        }
    }

    public class GetAccessTokenTask extends AsyncTask<String, Void, AccessToken> {

        EditText pin = (EditText) findViewById(R.id.password_pin);

        @Override
        protected AccessToken doInBackground(String... pin) {
            try {
                return TwitterSyncAdapter.TWITTER.getOAuthAccessToken(mRequestToken, pin[0]);
            } catch (TwitterException e) {
                Log.d("test", e.toString());
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
                //accountManager.addAccountExplicitly(account, accessToken.getTokenSecret(), Bundle.EMPTY);

                accountManager.setPassword(account, accessToken.getTokenSecret());
                accountManager.setAuthToken(account, type, accessToken.getToken());
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "認証できませんでした", Toast.LENGTH_LONG);
            }
        }
    }

}
