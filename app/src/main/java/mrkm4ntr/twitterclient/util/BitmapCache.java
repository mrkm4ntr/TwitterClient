package mrkm4ntr.twitterclient.util;

import android.graphics.Bitmap;
import android.util.LruCache;

public class BitmapCache {

    private static int cacheSize = 6 * 1024;

    private static LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }
    };

    public static Bitmap getImage(String key) {
        Bitmap bitmap = mCache.get(key);
        if (bitmap != null && bitmap.isRecycled()){
            return null;
        } else {
            return bitmap;
        }
    }

    public static void setImage(String key, Bitmap bitmap) {
        if (!hasImage(key) && bitmap != null) {
            mCache.put(key, bitmap);
        }
    }

    public static boolean hasImage(String key) {
        if (getImage(key) == null) {
            return false;
        } else {
            return true;
        }
    }

    public static void clear() {
        mCache.evictAll();
    }
}
