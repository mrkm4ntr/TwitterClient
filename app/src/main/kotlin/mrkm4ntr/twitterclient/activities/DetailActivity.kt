package mrkm4ntr.twitterclient.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu

import mrkm4ntr.twitterclient.R
import mrkm4ntr.twitterclient.views.DetailFragment

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val arguments = Bundle()
            arguments.putParcelable(DetailFragment.DETAIL_URI, intent.data)
            val fragment = DetailFragment()
            fragment.arguments = arguments

            supportFragmentManager.beginTransaction().add(R.id.status_detail_container, fragment).commit()
        }
    }

}
