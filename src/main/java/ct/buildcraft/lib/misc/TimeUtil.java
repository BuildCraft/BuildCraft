package ct.buildcraft.lib.misc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public enum TimeUtil {
    ;

    public static String formatNow() {
        return format(LocalDateTime.now());
    }

    public static String format(LocalDateTime time) {
        return time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
