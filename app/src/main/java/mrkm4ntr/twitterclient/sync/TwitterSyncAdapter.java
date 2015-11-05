package mrkm4ntr.twitterclient.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import mrkm4ntr.twitterclient.R;
import mrkm4ntr.twitterclient.data.TwitterContract;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class TwitterSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String CONSUMER_KEY = "xxx";
    private static final String CONSUMER_SECRET = "xxx";

    public static final String SYNC_FINISHED = "SyncFinished";
    public static final String EXTRA_SUCCEEDED = "SUCCEEDED";
    public static final String EXTRA_JUMP = "JUMP";

    private static final String LOG_TAG = TwitterSyncAdapter.class.getSimpleName();

    public static final Twitter TWITTER;
    private Context mContext;

    static {
        TWITTER = TwitterFactory.getSingleton();
        TWITTER.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
    }

    public TwitterSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(
            Account account, Bundle bundle, String s, ContentProviderClient
            contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        AccountManager accountManager = AccountManager.get(mContext);
        Intent intent = new Intent(SYNC_FINISHED);

        try {
            String token = accountManager.blockingGetAuthToken(account,
                    mContext.getString(R.string.sync_account_type), true);
            String tokenSecret = accountManager.getPassword(account);
            if (token != null && tokenSecret != null) {
                TWITTER.setOAuthAccessToken(new AccessToken(token, tokenSecret));
                List<Status> statuses = TWITTER.getHomeTimeline();

                for (Status status : statuses) {
                    ContentResolver resolver = mContext.getContentResolver();
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
                    resolver.insert(TwitterContract.StatusEntry.CONTENT_URI, contentValues);
                }
                intent.putExtra(EXTRA_SUCCEEDED, true);
            } else {
                intent.putExtra(EXTRA_JUMP, true);
            }
        } catch (AuthenticatorException e) {
            Log.d(LOG_TAG, e.toString());
            intent.putExtra(EXTRA_JUMP, true);
        } catch (OperationCanceledException | IOException | TwitterException e) {
            Log.d(LOG_TAG, e.toString());
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
        mContext.sendBroadcast(intent);

    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account account = null;
        Account[] accounts = accountManager.getAccountsByType(context.getString(R.string
                .sync_account_type));
        if (accounts.length == 0) {
            account = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
            accountManager.addAccountExplicitly(account, "", new Bundle());
            String authority = context.getString(R.string.content_authority);
            ContentResolver.setSyncAutomatically(account, authority, true);
        } else {
            account = accounts[0];
        }
        return account;
    }
}
