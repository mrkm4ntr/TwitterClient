package mrkm4ntr.twitterclient.extensions

import java.util.*

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