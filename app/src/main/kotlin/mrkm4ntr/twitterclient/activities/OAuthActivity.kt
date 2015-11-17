package mrkm4ntr.twitterclient.activities

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.data.TwitterContract
import mrkm4ntr.twitterclient.sync.TwitterSyncAdapter
import mrkm4ntr.twitterclient.views.StatusAdapter
import twitter4j.Twitter
import twitter4j.User
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken

class OAuthActivity : AppCompatActivity() {

    private var mRequestToken: RequestToken? = null
    private var mView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        mView = findViewById(R.id.oauth_layout)

        val pinText = findViewById(R.id.password_pin) as EditText
        val authButton = findViewById(R.id.button_auth) as Button

        authButton.setOnClickListener {
            val pin = pinText.text.toString()
            GetAccessTokenTask(pin).execute()
        }

        val openTwitterButton = findViewById(R.id.button_open_twitter) as Button
        openTwitterButton.setOnClickListener {
            GetRequestTokenTask().execute()
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    inner class GetRequestTokenTask : AsyncTask<Void, Void, RequestToken>() {

        override fun doInBackground(vararg params: Void): RequestToken? {
            try {
                val twitter = TwitterSyncAdapter.TWITTER
                twitter.oAuthAccessToken = null
                return twitter.oAuthRequestToken
            } catch (e: Exception) {
                Log.d(LOG_TAG, e.toString())
            }

            return null
        }

        override fun onPostExecute(requestToken: RequestToken?) {
            super.onPostExecute(requestToken)
            requestToken?.let {
                mRequestToken = it
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.authorizationURL)))
            } ?: Snackbar.make(mView,
                        applicationContext.getString(R.string.message_error_requestToken),
                        Snackbar.LENGTH_SHORT).show()
        }
    }

    inner class GetAccessTokenTask(private val mPin: String) : AsyncTask<Void, Void, AccessToken>() {

        private val SYNC_INTERVAL = 60 * 60L
        private val FLEX_TIME = SYNC_INTERVAL / 3

        override fun doInBackground(vararg params: Void): AccessToken? {
            try {
                return TwitterSyncAdapter.TWITTER.getOAuthAccessToken(mRequestToken, mPin)
            } catch (e: Exception) {
                Log.d(LOG_TAG, e.toString())
            }

            return null
        }

        override fun onPostExecute(accessToken: AccessToken?) {
            super.onPostExecute(accessToken)
            accessToken?.let {
                TwitterSyncAdapter.TWITTER.oAuthAccessToken = it
                val context = applicationContext
                val type = context.getString(R.string.sync_account_type)
                val account = Account(context.getString(R.string.app_name), type)
                val accountManager = AccountManager.get(application)
                accountManager.setPassword(account, it.tokenSecret)
                accountManager.setAuthToken(account, type, it.token)

                object: AsyncTask<Void, Void, User>() {
                    override fun doInBackground(vararg params: Void?): User? {
                        try {
                            TwitterSyncAdapter.TWITTER.oAuthAccessToken = AccessToken(it.token, it.tokenSecret)
                            return TwitterSyncAdapter.TWITTER.verifyCredentials()
                        } catch (e: Exception) {
                            return null
                        }
                    }

                    override fun onPostExecute(result: User?) {
                        result?.let {
                            // TODO: 事前チェック
                            val contentValues = ContentValues().apply {
                                put(TwitterContract.AccountEntry.COLUMN_NAME, it.name)
                                put(TwitterContract.AccountEntry.COLUMN_SCREEN_NAME, it.screenName)
                                put(TwitterContract.AccountEntry.COLUMN_PROFILE_IMAGE_URL, it.profileImageURL)
                                put(TwitterContract.AccountEntry.COLUMN_PROFILE_BANNER_URL, it.profileBannerMobileURL)
                            }
                            contentResolver.insert(TwitterContract.AccountEntry.CONTENT_URI, contentValues)
                        }
                    }
                }.execute()

                val request = SyncRequest.Builder()
                        .syncPeriodic(SYNC_INTERVAL, FLEX_TIME)
                        .setSyncAdapter(account, getString(R.string.content_authority))
                        .setExtras(Bundle())
                        .build()
                ContentResolver.requestSync(request)
                setResult(Activity.RESULT_OK)
                finish()
            } ?: Snackbar.make(mView, applicationContext.getString(R.string.message_error_auth),
                    Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {

        public val LOG_TAG = OAuthActivity::class.java.simpleName
    }

}
