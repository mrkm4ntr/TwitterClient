package mrkm4ntr.twitterclient.activities

import android.accounts.Account
import android.accounts.AccountManager
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.SyncRequest
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.data.TwitterContract
import mrkm4ntr.twitterclient.sync.TwitterSyncAdapter
import twitter4j.TwitterException
import twitter4j.User
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken

class OAuthActivity : AppCompatActivity() {

    private var mRequestToken: RequestToken? = null

    private val pinTextView by lazy { findViewById(R.id.password_pin) as EditText }
    private val oauthFormView by lazy { findViewById(R.id.oauth_form) }
    private val progressView by lazy { findViewById(R.id.auth_progress) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth)
        //val toolbar = findViewById(R.id.toolbar) as Toolbar
        //setSupportActionBar(toolbar)

        val authButton = findViewById(R.id.button_auth) as Button

        authButton.setOnClickListener {
            pinTextView.error = null
            val pin = pinTextView.text.toString()
            if (TextUtils.isEmpty(pin)) {
                pinTextView.error = "暗証番号を入力してください"
            } else {
                showProgress(true)
                GetAccessTokenTask(pin).execute()
            }
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

    fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        oauthFormView.visibility = if (show) View.GONE else View.VISIBLE
        oauthFormView.animate()
                .setDuration(shortAnimTime.toLong())
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        oauthFormView.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        progressView.visibility = if (show) View.VISIBLE else View.GONE
        progressView.animate()
                .setDuration(shortAnimTime.toLong())
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        progressView.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
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
            requestToken?.let {
                mRequestToken = it
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.authorizationURL)))
            } ?: Snackbar.make(oauthFormView,
                        applicationContext.getString(R.string.message_error_requestToken),
                        Snackbar.LENGTH_SHORT).show()
        }
    }

    inner class GetAccessTokenTask(private val pin: String) :
            AsyncTask<Void, Void, Pair<AccessToken?, Exception?>>() {

        private val SYNC_INTERVAL = 24 * 60 * 60L
        private val FLEX_TIME = SYNC_INTERVAL / 3

        // I need Either...
        override fun doInBackground(vararg params: Void): Pair<AccessToken?, Exception?> {
            try {
                //return Pair(TwitterSyncAdapter.TWITTER.getOAuthAccessToken(mRequestToken, pin), null)
                Thread.sleep(10000L)
                return Pair(null, Exception())
            } catch (e: Exception) {
                Log.d(LOG_TAG, e.toString())
                return Pair(null, e)
            }
        }

        override fun onPostExecute(accessToken: Pair<AccessToken?, Exception?>) {
            showProgress(false)
            accessToken.first?.let {
                val type = applicationContext.getString(R.string.sync_account_type)
                val account = Account(it.screenName, type)
                with(AccountManager.get(applicationContext)) {
                    addAccountExplicitly(account, "", Bundle().apply {
                        putString("tokenSecret", it.tokenSecret)
                    })
                    setAuthToken(account, type, it.token)
                }
            } ?: accessToken.second?.let {
                val message = when (it) {
                    is TwitterException -> applicationContext.getString(R.string.message_error_requestToken)
                    else -> "通信エラーです"
                }
                Snackbar.make(oauthFormView, message, Snackbar.LENGTH_SHORT).show()
            }
            /*accessToken?.let {
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
                                put(TwitterContract.AccountEntry.COLUMN_PROFILE_IMAGE_URL, it.biggerProfileImageURL)
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
            } ?: Snackbar.make(pinTextView, applicationContext.getString(R.string.message_error_auth),
                    Snackbar.LENGTH_SHORT).show()*/
        }
    }

    companion object {

        public val LOG_TAG = OAuthActivity::class.java.simpleName
    }

}
