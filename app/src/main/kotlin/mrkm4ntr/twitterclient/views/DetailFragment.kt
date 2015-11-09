package mrkm4ntr.twitterclient.views

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ShareActionProvider
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.data.TwitterContract

class DetailFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private var mShareActionProvider: ShareActionProvider? = null
    private var mStatus: String? = null
    private var mUri: Uri? = null

    private var mIconView: ImageView? = null
    private var mNameView: TextView? = null
    private var mScreenNameView: TextView? = null
    private var mLocationView: TextView? = null
    private var mBioView: TextView? = null
    private var mTextView: TextView? = null

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val arguments = arguments
        if (arguments != null) {
            mUri = arguments.getParcelable<Uri>(DETAIL_URI)
        }
        val rootView = inflater!!.inflate(R.layout.fragment_detail, container, false)
        mIconView = rootView.findViewById(R.id.detail_icon) as ImageView
        mNameView = rootView.findViewById(R.id.detail_name_textView) as TextView
        mScreenNameView = rootView.findViewById(R.id.detail_screenName_textView) as TextView
        mLocationView = rootView.findViewById(R.id.detail_userLocation_textView) as TextView
        mBioView = rootView.findViewById(R.id.detail_userBio_textView) as TextView
        mTextView = rootView.findViewById(R.id.detail_text_textView) as TextView
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        loaderManager.initLoader(DETAIL_LOADER, null, this)
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (this.isVisible) {
            inflater!!.inflate(R.menu.menu_detail_fragment, menu)
            val menuItem = menu!!.findItem(R.id.action_share)
            mShareActionProvider = MenuItemCompat.getActionProvider(menuItem) as ShareActionProvider
            if (mStatus != null) {
                mShareActionProvider!!.setShareIntent(createShareStatusIntent())
            }
        }
    }

    private fun createShareStatusIntent(): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_TEXT, mStatus!! + STATUS_SHARE_HASHTAG)
        return intent
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor>? {
        if (mUri != null) {
            return CursorLoader(activity, mUri, DETAIL_COLUMNS, null, null, null)
        }
        return null
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (data != null && data.moveToFirst()) {
            val profileImageUrl = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL))
            StatusAdapter.UpdateImageViewTask(mIconView!!, profileImageUrl).execute()
            val name = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_NAME))
            mNameView!!.text = name
            val screenName = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME))
            mScreenNameView!!.text = screenName
            val location = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_LOCATION))
            mLocationView!!.text = location
            val bio = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_USER_BIO))
            mBioView!!.text = bio
            val text = data.getString(data.getColumnIndex(
                    TwitterContract.StatusEntry.COLUMN_TEXT))
            mTextView!!.text = text
            mStatus = text

            if (mShareActionProvider != null) {
                mShareActionProvider!!.setShareIntent(createShareStatusIntent())
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

    companion object {

        private val LOG_TAG = DetailFragment::class.java.simpleName
        val DETAIL_URI = "URI"

        private val STATUS_SHARE_HASHTAG = "#TwitterClient"

        private val DETAIL_LOADER = 0

        private val DETAIL_COLUMNS = arrayOf(TwitterContract.StatusEntry._ID, TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL, TwitterContract.StatusEntry.COLUMN_USER_NAME, TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME, TwitterContract.StatusEntry.COLUMN_TEXT, TwitterContract.StatusEntry.COLUMN_USER_LOCATION, TwitterContract.StatusEntry.COLUMN_USER_BIO)
    }
}
