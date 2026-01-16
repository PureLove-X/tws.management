package tech.purelove.twsmanagement.player.hud;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Time {

    private static final int ticksPerHour = 1000;
    private static final double ticksPerMinute = 1000d / 60d;

    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ENGLISH);

    static String ticksToTime(long ticks) {
        ticks = (ticks + 6000) % 24000;

        calendar.setLenient(true);

        long hourTicks = ticks / ticksPerHour;
        return String.format("%s:%s", String.format("%02d", hourTicks),
                String.format("%02d", (int) Math.floor(ticks / ticksPerMinute) % 60));
    }
}
