package mrkm4ntr.twitterclient.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SyncResult
import android.os.Bundle
import android.util.Log

import java.io.IOException

import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.data.TwitterContract
import twitter4j.Status
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.User
import twitter4j.auth.AccessToken

class TwitterSyncAdapter(private val mContext: Context, autoInitialize: Boolean) : AbstractThreadedSyncAdapter(mContext, autoInitialize) {

    override fun onPerformSync(
            account: Account, bundle: Bundle, s: String, contentProviderClient: ContentProviderClient, syncResult: SyncResult) {
        Log.d(LOG_TAG, "Starting sync")
        val accountManager = AccountManager.get(mContext)
        val intent = Intent(SYNC_FINISHED)

        try {
            val token = accountManager.blockingGetAuthToken(account,
                    mContext.getString(R.string.sync_account_type), true)
            val tokenSecret = accountManager.getPassword(account)
            if (token != null && tokenSecret != null) {
                TWITTER.oAuthAccessToken = AccessToken(token, tokenSecret)
                val statuses = TWITTER.homeTimeline

                for (status in statuses) {
                    val resolver = mContext.contentResolver
                    val user = status.user
                    val contentValues = ContentValues()
                    contentValues.put(TwitterContract.StatusEntry._ID, status.id)
                    contentValues.put(TwitterContract.StatusEntry.COLUMN_TEXT, status.text)
                    contentValues.put(TwitterContract.StatusEntry.COLUMN_CREATE_AT,
                            status.createdAt.time)
                    contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_NAME, user.name)
                    contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL,
                            user.profileImageURL)
                    contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME,
                            user.screenName)
                    contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_LOCATION,
                            user.location)
                    contentValues.put(TwitterContract.StatusEntry.COLUMN_USER_BIO,
                            user.description)
                    resolver.insert(TwitterContract.StatusEntry.CONTENT_URI, contentValues)
                }
                intent.putExtra(EXTRA_SUCCEEDED, true)
            } else {
                intent.putExtra(EXTRA_JUMP, true)
            }
        } catch (e: AuthenticatorException) {
            Log.d(LOG_TAG, e.toString())
            intent.putExtra(EXTRA_JUMP, true)
        } catch (e: OperationCanceledException) {
            Log.d(LOG_TAG, e.toString())
        } catch (e: IOException) {
            Log.d(LOG_TAG, e.toString())
        } catch (e: TwitterException) {
            Log.d(LOG_TAG, e.toString())
        } catch (e: Exception) {
            Log.d(LOG_TAG, e.toString())
        }

        mContext.sendBroadcast(intent)

    }

    companion object {

        private val CONSUMER_KEY = "xxx"
        private val CONSUMER_SECRET = "xxx"

        val SYNC_FINISHED = "SyncFinished"
        val EXTRA_SUCCEEDED = "SUCCEEDED"
        val EXTRA_JUMP = "JUMP"

        private val LOG_TAG = TwitterSyncAdapter::class.java.simpleName

        val TWITTER: Twitter

        init {
            TWITTER = TwitterFactory.getSingleton()
            TWITTER.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET)
        }

        fun syncImmediately(context: Context) {
            val bundle = Bundle()
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
            ContentResolver.requestSync(getSyncAccount(context),
                    context.getString(R.string.content_authority), bundle)
        }

        fun getSyncAccount(context: Context): Account {
            val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
            var account: Account? = null
            val accounts = accountManager.getAccountsByType(context.getString(R.string.sync_account_type))
            if (accounts.size() == 0) {
                account = Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type))
                accountManager.addAccountExplicitly(account, "", Bundle())
                val authority = context.getString(R.string.content_authority)
                ContentResolver.setSyncAutomatically(account, authority, true)
            } else {
                account = accounts[0]
            }
            return account!!
        }
    }
}
