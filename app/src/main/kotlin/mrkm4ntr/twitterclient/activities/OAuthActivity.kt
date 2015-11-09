package mrkm4ntr.twitterclient.activities

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SyncRequest
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

import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.sync.TwitterSyncAdapter
import twitter4j.Twitter
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

        authButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val pin = pinText.text.toString()
                GetAccessTokenTask(pin).execute()
            }
        })

        val openTwitterButton = findViewById(R.id.button_open_twitter) as Button
        openTwitterButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                GetRequestTokenTask().execute()
            }
        })

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
            if (requestToken != null) {
                mRequestToken = requestToken
                startActivity(Intent(
                        Intent.ACTION_VIEW, Uri.parse(requestToken.authorizationURL)))
            } else {
                Snackbar.make(mView,
                        applicationContext.getString(R.string.message_error_requestToken),
                        Snackbar.LENGTH_SHORT).show()
            }
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
            if (accessToken != null) {
                TwitterSyncAdapter.TWITTER.oAuthAccessToken = accessToken
                val context = applicationContext
                val type = context.getString(R.string.sync_account_type)
                val account = Account(context.getString(R.string.app_name), type)
                val accountManager = AccountManager.get(application)
                accountManager.setPassword(account, accessToken.tokenSecret)
                accountManager.setAuthToken(account, type, accessToken.token)
                val request = SyncRequest.Builder().syncPeriodic(SYNC_INTERVAL, FLEX_TIME).setSyncAdapter(account, getString(R.string.content_authority)).setExtras(Bundle()).build()
                ContentResolver.requestSync(request)
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Snackbar.make(mView,
                        applicationContext.getString(R.string.message_error_auth),
                        Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    companion object {

        private val LOG_TAG = OAuthActivity::class.java.simpleName
    }

}
