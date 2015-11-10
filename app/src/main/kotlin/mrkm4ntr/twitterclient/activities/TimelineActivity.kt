package mrkm4ntr.twitterclient.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View

import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.views.DetailFragment
import mrkm4ntr.twitterclient.views.TimelineFragment

class TimelineActivity : AppCompatActivity(), TimelineFragment.Callback {

    private var mTwoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

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

    companion object {

        private val LOG_TAG = TimelineActivity::class.java.simpleName
        private val DETAIL_FRAGMENT_TAG = "DFTAG"
    }
}
