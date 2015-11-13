package mrkm4ntr.twitterclient.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.activities.OAuthActivity
import mrkm4ntr.twitterclient.data.TwitterContract
import mrkm4ntr.twitterclient.sync.TwitterSyncAdapter

class TimelineFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private val mStatusAdapter by lazy { StatusAdapter(activity, null, 0) }

    private val mSwipeRefreshLayout by lazy {
        view.findViewById(R.id.refreshLayout_timeline) as SwipeRefreshLayout
    }

    private val mListView by lazy { view.findViewById(R.id.listView_timeline) as ListView }
    private var mPosition = ListView.INVALID_POSITION

    private val mSyncFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mSwipeRefreshLayout.isRefreshing = false
            if (!intent.getBooleanExtra(TwitterSyncAdapter.EXTRA_SUCCEEDED, false)) {
                Snackbar.make(mListView, context.getString(R.string.message_error_sync),
                        Snackbar.LENGTH_SHORT).show()
                if (intent.getBooleanExtra(TwitterSyncAdapter.EXTRA_JUMP, false)) {
                    startActivity(Intent(context, OAuthActivity::class.java))
                }
            }
        }
    }

    private val mTimeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mStatusAdapter.notifyDataSetChanged()
        }
    }

    interface Callback {
        fun onItemSelected(uri: Uri)
    }

    override fun onCreateView(
            inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_timeline, container, false)

        savedInstanceState?.let {
            if (it.containsKey(SELECTED_KEY)) {
                mPosition = savedInstanceState.getInt(SELECTED_KEY)
            }
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        loaderManager.initLoader(TIMELINE_LOADER, Bundle(), this)
        savedInstanceState?.let {
            TwitterSyncAdapter.syncImmediately(activity)
            mSwipeRefreshLayout.isRefreshing = true
        }
        mSwipeRefreshLayout.setOnRefreshListener(this)
        mListView.adapter = mStatusAdapter
        mListView.setOnItemClickListener { parent, view, position, id ->
            val cursor = parent.getItemAtPosition(position) as Cursor
            (activity as Callback).onItemSelected(TwitterContract.StatusEntry.buildStatusUri(
                    cursor.getLong(cursor.getColumnIndex(TwitterContract.StatusEntry._ID))))
            mPosition = position
        }
        mListView.setOnScrollListener(object : EndlessScrollListener(mStatusAdapter) {
            override fun onLoadMore(maxId: Long, totalItemCount: Int): Boolean {
                TwitterSyncAdapter.syncImmediately(activity, maxId)
                return true
            }
        })
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val context = activity
        context.registerReceiver(mSyncFinishedReceiver, IntentFilter(TwitterSyncAdapter.SYNC_FINISHED))
        context.registerReceiver(mTimeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState!!.putInt(SELECTED_KEY, mPosition)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        val context = activity
        context.unregisterReceiver(mSyncFinishedReceiver)
        context.unregisterReceiver(mTimeTickReceiver)
    }

    override fun onCreateLoader(i: Int, bundle: Bundle): Loader<Cursor> {
        val sortOder = TwitterContract.StatusEntry.COLUMN_CREATE_AT + " DESC"
        return CursorLoader(activity,
                TwitterContract.StatusEntry.CONTENT_URI,
                STATUS_COLUMNS, null, null, sortOder)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        mStatusAdapter.swapCursor(data)
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition)
            mListView.setItemChecked(mPosition, true)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mStatusAdapter.swapCursor(null)
    }

    override fun onRefresh() {
        TwitterSyncAdapter.syncImmediately(activity)
    }

    companion object {

        val LOG_TAG = TimelineFragment::class.java.simpleName
        private val SELECTED_KEY = "selected_position"

        private val TIMELINE_LOADER = 0

        private val STATUS_COLUMNS = arrayOf(
                TwitterContract.StatusEntry._ID,
                TwitterContract.StatusEntry.COLUMN_TEXT,
                TwitterContract.StatusEntry.COLUMN_CREATE_AT,
                TwitterContract.StatusEntry.COLUMN_USER_NAME,
                TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL,
                TwitterContract.StatusEntry.COLUMN_USER_SCREEN_NAME
        )
    }
}
