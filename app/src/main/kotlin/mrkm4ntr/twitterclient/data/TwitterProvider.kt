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

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val cursor = when (uriMather.match(uri)) {
            STATUS -> {
                mOpenHelper!!.readableDatabase.query(
                        TwitterContract.StatusEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder)
            }
            STATUS_WITH_ID -> {
                mOpenHelper!!.readableDatabase.query(
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
        when (uriMather.match(uri)) {
            STATUS -> return TwitterContract.StatusEntry.CONTENT_TYPE
            STATUS_WITH_ID -> return TwitterContract.StatusEntry.CONTENT_ITEM_TYPE
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val db = mOpenHelper!!.writableDatabase

        val returnUri = when (uriMather.match(uri)) {
            STATUS -> {
                val _id = db.insert(TwitterContract.StatusEntry.TABLE_NAME, null, contentValues)
                if (_id > 0) {
                    TwitterContract.StatusEntry.buildStatusUri(_id)
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

        when (uriMather.match(uri)) {
            STATUS -> {
                db.beginTransaction()
                val returnCount = try {
                    val count = values.count {
                        db.insert(TwitterContract.StatusEntry.TABLE_NAME, null, it) != -1L
                    }
                    db.setTransactionSuccessful()
                    count
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

        private val uriMather = UriMatcher(UriMatcher.NO_MATCH).apply {
            val authority = TwitterContract.CONTENT_AUTHORITY
            addURI(authority, TwitterContract.PATH_STATUS, STATUS)
            addURI(authority, TwitterContract.PATH_STATUS + "/*", STATUS_WITH_ID)
        }

        private val STATUS = 100
        private val STATUS_WITH_ID = 101

    }
}
