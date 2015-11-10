package mrkm4ntr.twitterclient.views

import android.database.Cursor
import android.widget.AbsListView
import mrkm4ntr.twitterclient.data.TwitterContract

abstract class EndlessScrollListener(private val statusAdapter: StatusAdapter) : AbsListView.OnScrollListener {

    private var previousTotalItemCount = 0
    private var isLoading = true
    private var maxId = 0L

    override fun onScrollStateChanged(view: AbsListView?, firstVisibleItem: Int) {
        // Do nothing
    }

    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int,
                          totalItemCount: Int) {
        if (totalItemCount < previousTotalItemCount) {
            previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) {
                isLoading = true
            }
        }

        if (isLoading && totalItemCount > previousTotalItemCount) {
            isLoading = false
            previousTotalItemCount = totalItemCount
        }

        if (!isLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleItemCount)) {
            isLoading = true
            val cursor = statusAdapter.getItem(totalItemCount - 1) as Cursor
            maxId = cursor.run {
                getLong(getColumnIndex(TwitterContract.StatusEntry._ID))
            }
            isLoading = onLoadMore(maxId, totalItemCount)
        }
    }

    public abstract fun onLoadMore(maxId: Long, totalItemCount: Int): Boolean
}