package mrkm4ntr.twitterclient.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class TwitterAuthenticatorService extends Service {

    private TwitterAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new TwitterAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
