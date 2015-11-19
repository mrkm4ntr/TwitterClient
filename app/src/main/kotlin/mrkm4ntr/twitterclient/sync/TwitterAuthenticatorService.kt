package mrkm4ntr.twitterclient.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

class TwitterAuthenticatorService : Service() {

    private val authenticator by lazy { TwitterAuthenticator(this) }

    override fun onBind(intent: Intent): IBinder? {
        return authenticator.iBinder
    }
}
