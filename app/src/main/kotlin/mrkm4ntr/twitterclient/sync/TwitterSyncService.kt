package mrkm4ntr.twitterclient.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class TwitterSyncService : Service() {

    override fun onCreate() {
        Log.d("TwitterSyncService", "onCreate - TwitterSyncService")
        synchronized (SYNC_ADAPTER_LOCK) {
            if (sTwitterSyncAdapter == null) {
                sTwitterSyncAdapter = TwitterSyncAdapter(applicationContext, true)
            }
        }
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder? {
        return sTwitterSyncAdapter!!.syncAdapterBinder
    }

    companion object {

        private val SYNC_ADAPTER_LOCK = Object()
        private var sTwitterSyncAdapter: TwitterSyncAdapter? = null
    }
}
