package mrkm4ntr.twitterclient.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

class TwitterProvider : ContentProvider() {
    private var mOpenHelper: TwitterDbHelper? = null

    override fun onCreate(): Boolean {
        mOpenHelper = TwitterDbHelper(context)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?,
                       sortOrder: String?): Cursor? {
        val cursor: Cursor
        when (URI_MATCHER.match(uri)) {
            STATUS -> {
                cursor = mOpenHelper!!.readableDatabase.query(
                        TwitterContract.StatusEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder)
            }
            STATUS_WITH_ID -> {
                cursor = mOpenHelper!!.readableDatabase.query(
                        TwitterContract.StatusEntry.TABLE_NAME, projection,
                        TwitterContract.StatusEntry._ID + " = ? ",
                        arrayOf<String>(uri.pathSegments[1]), null, null, sortOrder)
            }
            else -> throw UnsupportedOperationException("Unknown uri:" + uri)
        }
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        when (URI_MATCHER.match(uri)) {
            STATUS -> return TwitterContract.StatusEntry.CONTENT_TYPE
            STATUS_WITH_ID -> return TwitterContract.StatusEntry.CONTENT_ITEM_TYPE
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val db = mOpenHelper!!.writableDatabase
        val match = URI_MATCHER.match(uri)
        val returnUri: Uri

        when (match) {
            STATUS -> {
                val _id = db.insert(TwitterContract.StatusEntry.TABLE_NAME, null, contentValues)
                if (_id > 0) {
                    returnUri = TwitterContract.StatusEntry.buildStatusUri(_id)
                } else {
                    throw SQLException("Failed to insert row into " + uri)
                }
            }
            else -> throw UnsupportedOperationException("Unknown uri:" + uri)
        }
        context!!.contentResolver.notifyChange(uri, null)
        return returnUri
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        val db = mOpenHelper!!.writableDatabase
        val match = URI_MATCHER.match(uri)
        when (match) {
            STATUS -> {
                db.beginTransaction()
                var returnCount = 0
                try {
                    for (value in values) {
                        val id = db.insert(TwitterContract.StatusEntry.TABLE_NAME, null, value)
                        if (id != -1L) {
                            returnCount++
                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                context!!.contentResolver.notifyChange(uri, null)
                return returnCount
            }
            else -> return super.bulkInsert(uri, values)
        }
    }

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, contentValues: ContentValues?, s: String?, strings: Array<String>?): Int {
        return 0
    }

    companion object {

        private val URI_MATCHER = buildUriMatcher()

        private val STATUS = 100
        private val STATUS_WITH_ID = 101

        private fun buildUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            val authority = TwitterContract.CONTENT_AUTHORITY
            matcher.addURI(authority, TwitterContract.PATH_STATUS, STATUS)
            matcher.addURI(authority, TwitterContract.PATH_STATUS + "/*", STATUS_WITH_ID)
            return matcher
        }
    }
}
