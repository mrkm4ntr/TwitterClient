package mrkm4ntr.twitterclient.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

public class TwitterProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private TwitterDbHelper mOpenHelper;

    private static final int STATUS = 100;
    private static final int STATUS_WITH_ID = 101;
    private static final int USER = 200;
    private static final int USER_WITH_ID = 201;

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = TwitterContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, TwitterContract.PATH_STATUS, STATUS);
        matcher.addURI(authority, TwitterContract.PATH_STATUS + "/*", STATUS_WITH_ID);
        // TODO implement
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new TwitterDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor cursor;
        switch (URI_MATCHER.match(uri)) {
            case STATUS: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        TwitterContract.StatusEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            }
            case STATUS_WITH_ID: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        TwitterContract.StatusEntry.TABLE_NAME, projection,
                        TwitterContract.StatusEntry._ID + " = ? ",
                        new String[]{ uri.getPathSegments().get(1) }, null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case STATUS:
                return TwitterContract.StatusEntry.CONTENT_TYPE;
            case STATUS_WITH_ID:
                return TwitterContract.StatusEntry.CONTENT_ITEM_TYPE;
            case USER_WITH_ID:
                return TwitterContract.UserEntry.CONTENT_ITEM_TYPE;
            // TODO implement
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        Uri returnUri;

        switch (match) {
            case STATUS: {
                long _id = db.insert(TwitterContract.StatusEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = TwitterContract.StatusEntry.buildStatusUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case USER: {
                long _id = db.insert(TwitterContract.UserEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = TwitterContract.UserEntry.buildUserUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw  new UnsupportedOperationException("Unknown uri:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case USER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long id = db.insert(TwitterContract.UserEntry.TABLE_NAME, null, value);
                        if (id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
