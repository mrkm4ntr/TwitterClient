package mrkm4ntr.twitterclient.views

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView

import java.io.InputStream
import java.net.URL
import java.util.Date

import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.data.TwitterContract
import mrkm4ntr.twitterclient.util.BitmapCache
import mrkm4ntr.twitterclient.util.Utility

class StatusAdapter(context: Context, c: Cursor?, flags: Int) : CursorAdapter(context, c, flags) {

    class ViewHolder(view: View) {
        val iconView: ImageView
        val nameView: TextView
        val textView: TextView
        val createdAtView: TextView

        init {
            iconView = view.findViewById(R.id.list_item_icon) as ImageView
            nameView = view.findViewById(R.id.list_item_name_textview) as TextView
            textView = view.findViewById(R.id.list_item_text_textview) as TextView
            createdAtView = view.findViewById(R.id.list_item_createdAt) as TextView
        }
    }

    override fun newView(context: Context, cursor: Cursor, viewGroup: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_status, viewGroup, false)
        val viewHolder = ViewHolder(view)
        view.tag = viewHolder
        return view
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val viewHolder = view.tag as ViewHolder
        val profileImageUrl = cursor.getString(cursor.getColumnIndex(
                TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL))
        val userName = cursor.getString(cursor.getColumnIndex(
                TwitterContract.StatusEntry.COLUMN_USER_NAME))
        val screenName = cursor.getString(cursor.getColumnIndex(
                TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME))
        val text = cursor.getString(cursor.getColumnIndex(
                TwitterContract.StatusEntry.COLUMN_TEXT))
        val createdAt = cursor.getLong(cursor.getColumnIndex(
                TwitterContract.StatusEntry.COLUMN_CREATE_AT))
        viewHolder.nameView.text = userName + "@" + screenName
        viewHolder.textView.text = text
        viewHolder.createdAtView.text = Utility.datetimeAgo(Date(createdAt))
        UpdateImageViewTask(viewHolder.iconView, profileImageUrl).execute()
    }

    class UpdateImageViewTask(private val mImageView: ImageView, private val mImageUrl: String) : AsyncTask<Void, Void, Bitmap>() {

        override fun doInBackground(vararg params: Void): Bitmap? {
            try {
                val input = URL(mImageUrl).openStream()
                return BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPreExecute() {
            val bitmap = BitmapCache.getImage(mImageUrl)
            if (bitmap != null) {
                mImageView.setImageBitmap(bitmap)
                cancel(true)
            }
        }

        override fun onPostExecute(bitmap: Bitmap) {
            mImageView.setImageBitmap(bitmap)
            BitmapCache.setImage(mImageUrl, bitmap)
        }
    }

}
