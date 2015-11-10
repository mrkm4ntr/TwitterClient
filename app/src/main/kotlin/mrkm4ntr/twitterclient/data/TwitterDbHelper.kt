package mrkm4ntr.twitterclient.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TwitterDbHelper(context: Context) : SQLiteOpenHelper(
        context, TwitterDbHelper.DATABASE_NAME, null, TwitterDbHelper.DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_STATUS_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TwitterContract.StatusEntry.TABLE_NAME)
        onCreate(sqLiteDatabase)
    }

    companion object {

        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "twitter.db"

        private val SQL_CREATE_STATUS_TABLE = """
            CREATE TABLE ${TwitterContract.StatusEntry.TABLE_NAME} (
                ${TwitterContract.StatusEntry._ID} INTEGER PRIMARY KEY ON CONFLICT REPLACE,
                ${TwitterContract.StatusEntry.COLUMN_CREATE_AT} TEXT NOT NULL,
                ${TwitterContract.StatusEntry.COLUMN_TEXT} INTEGER NOT NULL,
                ${TwitterContract.StatusEntry.COLUMN_USER_NAME} TEXT NOT NULL,
                ${TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL} TEXT NOT NULL,
                ${TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME} TEXT NOT NULL,
                ${TwitterContract.StatusEntry.COLUMN_USER_LOCATION} TEXT NOT NULL,
                ${TwitterContract.StatusEntry.COLUMN_USER_BIO} TEXT NOT NULL
            )"""
    }
}