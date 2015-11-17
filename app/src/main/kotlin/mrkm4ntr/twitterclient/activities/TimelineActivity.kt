package mrkm4ntr.twitterclient.activities

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.data.TwitterContract
import mrkm4ntr.twitterclient.extensions.accountId
import mrkm4ntr.twitterclient.views.DetailFragment
import mrkm4ntr.twitterclient.views.StatusAdapter
import mrkm4ntr.twitterclient.views.TimelineFragment

class TimelineActivity : AppCompatActivity(), TimelineFragment.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    private var mTwoPane: Boolean = false
    private val drawerLayout by lazy { findViewById(R.id.drawerLayout) as DrawerLayout }
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        //val navigationView = findViewById(R.id.navigation_view) as NavigationView
        //navigationView.setNavigationItemSelectedListener(this);

        mDrawerToggle = ActionBarDrawerToggle(this, drawerLayout,
                // TODO change string id
                R.string.button_open_twitter,
                R.string.button_open_twitter)
        drawerLayout.setDrawerListener(mDrawerToggle)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar.setHomeButtonEnabled(true)
        mDrawerToggle!!.syncState();

        if (findViewById(R.id.status_detail_container) != null) {
            mTwoPane = true
            savedInstanceState?.let {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.status_detail_container, DetailFragment(), DETAIL_FRAGMENT_TAG)
                        .commit()
            }
        } else {
            mTwoPane = false
        }

        val fab = findViewById(R.id.fab) as FloatingActionButton
        val intent = Intent(this, TweetActivity::class.java)
        fab.setOnClickListener { view -> startActivity(intent) }

        /*accountId?.let {
            with(contentResolver.query(TwitterContract.AccountEntry.buildAccountUri(it), null, null, null, null)) {
                if (moveToFirst()) {
                    val selfIconView = findViewById(R.id.navigation_icon) as ImageView
                    val profileImageURL = getString(getColumnIndex(TwitterContract.AccountEntry.COLUMN_PROFILE_IMAGE_URL))
                    StatusAdapter.UpdateImageViewTask(selfIconView, profileImageURL).execute()
                    val selfBackgroundView = findViewById(R.id.navigation_background) as ImageView
                    val profileBackgroundImageURL = getString(getColumnIndex(TwitterContract.AccountEntry.COLUMN_PROFILE_BACKGROUND_IMAGE_URL))
                    StatusAdapter.UpdateImageViewTask(selfBackgroundView, profileBackgroundImageURL).execute()
                    with(findViewById(R.id.navigation_name_textView) as TextView) {
                        text = getString(getColumnIndex(TwitterContract.AccountEntry.COLUMN_NAME))
                    }
                    with(findViewById(R.id.navitation_screenName_textView) as TextView) {
                        text = "@${getString(getColumnIndex(TwitterContract.AccountEntry.COLUMN_SCREEN_NAME))}"
                    }
                }
            }
        }*/

        supportLoaderManager.initLoader<Cursor>(1, Bundle(), this)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
            //if (item!!.itemId == android.support.v7.appcompat.R.id.home) {
        return mDrawerToggle!!.onOptionsItemSelected(item);
        //}
        //return super.onOptionsItemSelected(item);
    }

    override fun onItemSelected(uri: Uri) {
        if (mTwoPane) {
            val fragment = DetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DetailFragment.DETAIL_URI, uri)
                }
            }
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.status_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit()
        } else {
            val intent = Intent(this, DetailActivity::class.java).setData(uri)
            startActivity(intent)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor>? {
        accountId?.let {
            return CursorLoader(this, TwitterContract.AccountEntry.buildAccountUri(it), null, null, null, null)
        }
        return null
    }

    override fun onLoaderReset(loader: Loader<Cursor>?) {
        // Do nothing
    }

    override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
        data?.run {
            if (moveToFirst()) {
                val selfIconView = findViewById(R.id.navigation_icon) as ImageView
                val profileImageURL = getString(getColumnIndex(TwitterContract.AccountEntry.COLUMN_PROFILE_IMAGE_URL))
                StatusAdapter.UpdateImageViewTask(selfIconView, profileImageURL).execute()
                val selfBackgroundView = findViewById(R.id.navigation_background) as ImageView
                val profileBackgroundImageURL = getString(getColumnIndex(TwitterContract.AccountEntry.COLUMN_PROFILE_BACKGROUND_IMAGE_URL))
                StatusAdapter.UpdateImageViewTask(selfBackgroundView, profileBackgroundImageURL).execute()
                with(findViewById(R.id.navigation_name_textView) as TextView) {
                    text = getString(getColumnIndex(TwitterContract.AccountEntry.COLUMN_NAME))
                }
                with(findViewById(R.id.navitation_screenName_textView) as TextView) {
                    text = "@${getString(getColumnIndex(TwitterContract.AccountEntry.COLUMN_SCREEN_NAME))}"
                }
            }
        }
    }

    companion object {

        private val LOG_TAG = TimelineActivity::class.java.simpleName
        private val DETAIL_FRAGMENT_TAG = "DFTAG"
    }
}
