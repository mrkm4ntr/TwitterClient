package mrkm4ntr.twitterclient.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TwitterDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "twitter.db";

    public TwitterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String SQL_CREATE_STATUS_TABLE =
            String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY ON CONFLICT REPLACE, %s TEXT NOT NULL, %s INTEGER NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL)",
                    TwitterContract.StatusEntry.TABLE_NAME, TwitterContract.StatusEntry._ID,
                    TwitterContract.StatusEntry.COLUMN_CREATE_AT,
                    TwitterContract.StatusEntry.COLUMN_TEXT,
                    TwitterContract.StatusEntry.COLUMN_USER_NAME,
                    TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL);

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_STATUS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TwitterContract.StatusEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
