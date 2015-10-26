package mrkm4ntr.twitterclient.data;

import android.net.Uri;

/**
 * Created by Shintaro on 2015/10/26.
 */
public class TweetContract {
    public static final String CONTENT_AUTHORITY = "mrkm4ntr.twitterclient";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
}
