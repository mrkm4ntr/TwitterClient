package mrkm4ntr.twitterclient.views

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.AsyncTask
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView

import java.net.URL
import java.util.Date

import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.data.TwitterContract
import mrkm4ntr.twitterclient.extensions.applyHttpLink
import mrkm4ntr.twitterclient.extensions.datetimeAgo
import mrkm4ntr.twitterclient.util.BitmapCache

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
        cursor.run {
            val profileImageUrl = getString(getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL))
            val userName = getString(getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_NAME))
            val screenName = getString(getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME))
            val text = getString(getColumnIndex(TwitterContract.StatusEntry.COLUMN_TEXT))
            val createdAt = getLong(getColumnIndex(TwitterContract.StatusEntry.COLUMN_CREATE_AT))
            viewHolder.nameView.text = SpannableStringBuilder().apply {
                append(userName)
                setSpan(StyleSpan(Typeface.BOLD), 0, userName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                append("@")
                append(screenName)
            }
            viewHolder.textView.text = text
            viewHolder.textView.applyHttpLink(context)
            viewHolder.createdAtView.text = Date(createdAt).datetimeAgo()
            UpdateImageViewTask(viewHolder.iconView, profileImageUrl).execute()
        }
    }

    class UpdateImageViewTask(private val mImageView: ImageView, private val mImageUrl: String) :
            AsyncTask<Void, Void, Bitmap>() {

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
            bitmap?.let {
                mImageView.setImageBitmap(it)
                cancel(true)
            }
        }

        override fun onPostExecute(bitmap: Bitmap) {
            mImageView.setImageBitmap(bitmap)
            BitmapCache.setImage(mImageUrl, bitmap)
        }
    }

}
