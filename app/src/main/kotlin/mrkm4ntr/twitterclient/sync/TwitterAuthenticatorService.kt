package mrkm4ntr.twitterclient.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

class TwitterAuthenticatorService : Service() {

    private var mAuthenticator: TwitterAuthenticator? = null

    override fun onCreate() {
        mAuthenticator = TwitterAuthenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mAuthenticator!!.iBinder
    }
}
