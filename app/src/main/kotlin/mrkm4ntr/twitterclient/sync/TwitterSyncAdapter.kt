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
import twitter4j.*
import twitter4j.auth.AccessToken

class TwitterSyncAdapter(private val mContext: Context, autoInitialize: Boolean) : AbstractThreadedSyncAdapter(mContext, autoInitialize) {

    override fun onPerformSync(account: Account, bundle: Bundle, s: String,
                               contentProviderClient: ContentProviderClient,syncResult: SyncResult) {
        Log.d(LOG_TAG, "Starting sync")
        val accountManager = AccountManager.get(mContext)
        val intent = Intent(SYNC_FINISHED)
        val resolver = mContext.contentResolver

        val maxId: Long? = if (bundle.containsKey("maxid")) bundle.getLong("maxid") else null

        try {
            val token = accountManager.blockingGetAuthToken(account,
                    mContext.getString(R.string.sync_account_type), true)
            val tokenSecret = accountManager.getPassword(account)
            token?.run {
                tokenSecret?.run {
                    TWITTER.oAuthAccessToken = AccessToken(token, tokenSecret)
                    maxId?.run {
                        TWITTER.getHomeTimeline(Paging().maxId(maxId))
                    } ?: TWITTER.homeTimeline
                }
            }?.forEach {
                val user = it.user
                val contentValues = ContentValues()
                contentValues.put(TwitterContract.StatusEntry._ID, it.id)
                contentValues.put(TwitterContract.StatusEntry.COLUMN_TEXT, it.text)
                contentValues.put(TwitterContract.StatusEntry.COLUMN_CREATE_AT,
                        it.createdAt.time)
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
            }?.let {
                intent.putExtra(EXTRA_SUCCEEDED, true)
            } ?: intent.putExtra(EXTRA_JUMP, true)
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

        fun syncImmediately(context: Context, maxId : Long? = null) {
            val bundle = Bundle().apply {
                putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
                putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                maxId?.let { putLong("maxid", it) }
            }
            ContentResolver.requestSync(getSyncAccount(context),
                    context.getString(R.string.content_authority), bundle)
        }

        fun getSyncAccount(context: Context): Account {
            val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
            val accounts = accountManager.getAccountsByType(context.getString(R.string.sync_account_type))
            return if (accounts.size == 0) {
                val account = Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type))
                accountManager.addAccountExplicitly(account, "", Bundle())
                val authority = context.getString(R.string.content_authority)
                ContentResolver.setSyncAutomatically(account, authority, true)
                account
            } else {
                accounts[0]
            }
        }
    }
}
