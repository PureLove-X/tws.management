package tech.purelove.twsmanagement.announcements;

import java.util.ArrayList;
import java.util.List;

public final class AnnouncementParser {

    private enum Mode {
        SCHEDULE,
        BROADCAST
    }

    private AnnouncementParser() {}

    public static void parse(String content, AnnouncementRegistry registry) {
        Mode mode = null;
        String currentName = null;
        List<String> currentLines = new ArrayList<>();

        for (String raw : content.split("\\R")) {
            String line = raw.trim();

            // Skip comments / empty
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Mode switches
            if (line.equalsIgnoreCase("@Schedule")) {
                flush(registry, mode, currentName, currentLines);
                mode = Mode.SCHEDULE;
                currentName = null;
                continue;
            }

            if (line.equalsIgnoreCase("@Broadcast")) {
                flush(registry, mode, currentName, currentLines);
                mode = Mode.BROADCAST;
                currentName = null;
                continue;
            }

            // New announcement
            if (line.startsWith("[") && line.endsWith("]")) {
                flush(registry, mode, currentName, currentLines);
                currentName = line.substring(1, line.length() - 1).trim();
                continue;
            }

            // Message line
            if (mode == null || currentName == null) {
                continue;
            }

            currentLines.add(raw);
        }

        flush(registry, mode, currentName, currentLines);
    }

    private static void flush(
            AnnouncementRegistry registry,
            Mode mode,
            String name,
            List<String> lines
    ) {
        if (mode == null || name == null || lines.isEmpty()) {
            lines.clear();
            return;
        }

        registry.register(new Announcement(
                name,
                lines,
                mode == Mode.SCHEDULE
        ));

        lines.clear();
    }
}
