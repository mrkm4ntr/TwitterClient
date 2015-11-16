package mrkm4ntr.twitterclient.data

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

object TwitterContract {

    val CONTENT_AUTHORITY = "mrkm4ntr.twitterclient"

    val BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY)

    val PATH_STATUS = "statuses"

    class StatusEntry : BaseColumns {
        companion object {
            // KT-3180
            val _ID = BaseColumns._ID
            val _COUNT = BaseColumns._COUNT

            val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATUS).build()

            val CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATUS
            val CONTENT_ITEM_TYPE =ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATUS

            val TABLE_NAME = "statuses"

            val COLUMN_TEXT = "text"
            val COLUMN_CREATE_AT = "create_at"
            val COLUMN_USER_NAME = "user_name"
            val COLUMN_USER_PROFILE_IMAGE_URL = "user_profile_image_url"
            val COLUMN_USER_SCREEN_NAME = "user_screen_name"
            val COLUMN_USER_LOCATION = "user_location"
            val COLUMN_USER_BIO = "user_bio"

            fun buildStatusUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }
        }
    }

    val PATH_ACCOUNT = "accounts"

    class AccountEntry : BaseColumns {
        companion object {
            // KT-3180
            val _ID = BaseColumns._ID
            val _COUNT = BaseColumns._COUNT

            val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ACCOUNT).build()

            val CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATUS

            val TABLE_NAME = "accounts"

            val COLUMN_NAME = "name"
            val COLUMN_SCREEN_NAME = "screen_name"
            val COLUMN_PROFILE_IMAGE_URL = "profile_image_url"
            val COLUMN_PROFILE_BACKGROUND_IMAGE_URL = "profile_background_image_url"

            fun buildAccountUri(id: Long): Uri {
                return ContentUris.withAppendedId(CONTENT_URI, id)
            }
        }
    }
}
