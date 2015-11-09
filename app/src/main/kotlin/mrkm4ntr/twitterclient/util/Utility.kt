package mrkm4ntr.twitterclient.util

import java.util.Collections
import java.util.Date
import java.util.LinkedHashMap

object Utility {

    private val TIME_UNITS: Map<String, Long>

    init {
        val timeUnits = LinkedHashMap<String, Long>()
        timeUnits.put("year", 1000L * 60 * 60 * 24 * 365)
        timeUnits.put("month", 1000L * 60 * 60 * 24 * 30)
        timeUnits.put("day", 1000L * 60 * 60 * 24)
        timeUnits.put("hour", 1000L * 60 * 60)
        timeUnits.put("minute", 1000L * 60)
        TIME_UNITS = Collections.unmodifiableMap(timeUnits)
    }

    fun datetimeAgo(date: Date): String {
        val duration = Date().time - date.time
        for (entry in TIME_UNITS.entries) {
            val value = (duration / entry.value).toInt()
            if (value > 0) {
                return "$value ${entry.key} ${if (value > 1) "s" else ""} ago"
            }
        }
        return "just now"
    }
}
