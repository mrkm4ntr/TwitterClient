package mrkm4ntr.twitterclient.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Shintaro on 2015/10/26.
 */
public class TwitterSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = TwitterSyncAdapter.class.getSimpleName();

    public TwitterSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(
            Account account, Bundle bundle, String s, ContentProviderClient
            contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        // TODO HTTP access and call bulkInsert.
    }
}
