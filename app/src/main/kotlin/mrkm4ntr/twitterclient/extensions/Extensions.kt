package mrkm4ntr.twitterclient.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.widget.TextView
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.LinkBuilder
import java.util.*
import java.util.regex.Pattern

val timeUnits = listOf(
        Pair(1000L * 60, "minute"),
        Pair(1000L * 60 * 60, "hour"),
        Pair(1000L * 60 * 60 * 24, "day"),
        Pair(1000L * 60 * 60 * 24 * 30, "month"),
        Pair(1000L * 60 * 60 * 24 * 365, "year")
).reversed()

fun Date.datetimeAgo(): String {
    val duration = Date().time - this.time
    timeUnits.find {
        duration / it.first > 0
    }?.let {
        val value = (duration / it.first).toInt()
        return "$value ${it.second}${if (value > 1) "s" else ""} ago"
    }
    return "just now"
}

val httpPattern = Pattern.compile("(http|https):([^\\x00-\\x20()\"<>\\x7F-\\xFF])*", Pattern.CASE_INSENSITIVE)

fun TextView.applyHttpLink(context: Context) {
    val link = Link(httpPattern).setOnClickListener { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
    LinkBuilder.on(this).addLink(link).build()
}

var Context.accountId: Long?
    get() = PreferenceManager.getDefaultSharedPreferences(this).run {
        if (contains("accountId")) getLong("accountId", 0) else null
    }
    set(accountId) = PreferenceManager.getDefaultSharedPreferences(this).edit().run {
        accountId?.let {
            putLong("accountId", it)
        }
        apply()
    }