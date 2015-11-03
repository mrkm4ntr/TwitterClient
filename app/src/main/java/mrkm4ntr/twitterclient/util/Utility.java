package mrkm4ntr.twitterclient.util;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Utility {

    private static final Map<String, Long> TIME_UNITS;

    static {
        Map<String, Long> timeUnits = new LinkedHashMap<>();
        timeUnits.put("year", 1000L * 60 * 60 * 24 * 365);
        timeUnits.put("month", 1000L * 60 * 60 * 24 * 30);
        timeUnits.put("day", 1000L * 60 * 60 * 24);
        timeUnits.put("hour", 1000L * 60 * 60);
        timeUnits.put("minute", 1000L * 60);
        TIME_UNITS = Collections.unmodifiableMap(timeUnits);
    }

    public static String datetimeAgo(Date date) {
        long duration = new Date().getTime() - date.getTime();
        for (Map.Entry<String, Long> entry : TIME_UNITS.entrySet()) {
            int value = (int) (duration / entry.getValue());
            if (value > 0) {
                return value + " " + entry.getKey() + (value > 1 ? "s" : "") + " ago";
            }
        }
        return "just now";
    }
}
