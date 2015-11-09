package mrkm4ntr.twitterclient.util

import android.graphics.Bitmap
import android.util.LruCache

object BitmapCache {

    private val cacheSize = 6 * 1024

    private val mCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.rowBytes * bitmap.height / 1024
        }
    }

    fun getImage(key: String): Bitmap? {
        val bitmap = mCache.get(key)
        if (bitmap != null && bitmap.isRecycled) {
            return null
        } else {
            return bitmap
        }
    }

    fun setImage(key: String, bitmap: Bitmap?) {
        if (!hasImage(key) && bitmap != null) {
            mCache.put(key, bitmap)
        }
    }

    fun hasImage(key: String): Boolean {
        if (getImage(key) == null) {
            return false
        } else {
            return true
        }
    }

    fun clear() {
        mCache.evictAll()
    }
}
