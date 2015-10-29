package mrkm4ntr.twitterclient.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class TwitterSyncService extends Service {

    private static final Object SYNC_ADAPTER_LOCK = new Object();
    private static TwitterSyncAdapter sTwitterSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("TwitterSyncService", "onCreate - TwitterSyncService");
        synchronized (SYNC_ADAPTER_LOCK) {
            if (sTwitterSyncAdapter == null) {
                sTwitterSyncAdapter = new TwitterSyncAdapter(getApplicationContext(), true);
            }
        }
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sTwitterSyncAdapter.getSyncAdapterBinder();
    }
}
