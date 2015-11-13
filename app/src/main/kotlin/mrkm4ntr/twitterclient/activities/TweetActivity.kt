package mrkm4ntr.twitterclient.activities

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Dialog
import android.app.DialogFragment
import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView

import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.data.TwitterContract
import mrkm4ntr.twitterclient.sync.TwitterSyncAdapter
import mrkm4ntr.twitterclient.util.BitmapCache
import mrkm4ntr.twitterclient.views.ProgressDialogFragment
import mrkm4ntr.twitterclient.views.StatusAdapter
import twitter4j.Status
import twitter4j.Twitter
import twitter4j.User
import twitter4j.auth.AccessToken

class TweetActivity : AppCompatActivity() {

    private var mLayout: View? = null
    private var mTweetButton: Button? = null
    private var mEditText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)

        mLayout = findViewById(R.id.layout_post_tweet)

        mEditText = findViewById(R.id.editText) as EditText
        mTweetButton = findViewById(R.id.button_tweet) as Button

        mEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.length == 0) {
                    mTweetButton!!.isEnabled = false
                } else {
                    mTweetButton!!.isEnabled = true
                }
            }
        })

        mTweetButton!!.setOnClickListener {
            val status = mEditText!!.text.toString()
            TweetTask(status, applicationContext).execute()
        }

        if (mEditText!!.text.length == 0) {
            mTweetButton!!.isEnabled = false
        }
    }

    internal inner class TweetTask(private val mStatus: String, private val mContext: Context) :
            AsyncTask<Void, Void, Status>() {
        private var mDialogFragment: DialogFragment? = null

        override fun doInBackground(vararg voids: Void): twitter4j.Status? {
            try {
                val accountManager = AccountManager.get(mContext)
                val accounts = accountManager.getAccountsByType(
                        mContext.getString(R.string.sync_account_type))
                if (accounts.size > 0) {
                    val account = accounts[0]
                    val twitter = TwitterSyncAdapter.TWITTER
                    val token = accountManager.blockingGetAuthToken(account,
                            mContext.getString(R.string.sync_account_type), true)
                    val tokenSecret = accountManager.getPassword(account)
                    twitter.oAuthAccessToken = AccessToken(token, tokenSecret)
                    return twitter.updateStatus(mStatus)
                }
            } catch (e: Exception) {
                Log.d(LOG_TAG, e.toString())
            }

            return null
        }

        override fun onPreExecute() {
            mDialogFragment = ProgressDialogFragment.newInstance(
                    getString(R.string.message_progress_tweet))
            mDialogFragment!!.show(this@TweetActivity.fragmentManager, "dialog")
        }

        override fun onPostExecute(status: twitter4j.Status?) {
            status?.let {
                val user = it.user
                val contentValues = ContentValues().apply {
                    put(TwitterContract.StatusEntry._ID, it.id)
                    put(TwitterContract.StatusEntry.COLUMN_TEXT, it.text)
                    put(TwitterContract.StatusEntry.COLUMN_CREATE_AT, it.createdAt.time)
                    put(TwitterContract.StatusEntry.COLUMN_USER_NAME, user.name)
                    put(TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL, user.profileImageURL)
                    put(TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME, user.screenName)
                    put(TwitterContract.StatusEntry.COLUMN_USER_LOCATION, user.location)
                    put(TwitterContract.StatusEntry.COLUMN_USER_BIO, user.description)
                }
                contentResolver.insert(TwitterContract.StatusEntry.CONTENT_URI, contentValues)
                finish()
            } ?: {
                if (mDialogFragment!!.showsDialog) {
                    val dialog = mDialogFragment!!.dialog
                    dialog?.dismiss()
                }
                Snackbar.make(mLayout, R.string.message_error_post_tweet,
                        Snackbar.LENGTH_SHORT).show()
            }()
        }
    }

    companion object {

        private val LOG_TAG = TweetActivity::class.java.simpleName
    }

}
