package tech.purelove.twsmanagement.announcements;

import java.util.List;

public record Announcement(
        String name,
        List<String> lines,
        boolean scheduled
) {
    public Announcement {
        lines = List.copyOf(lines);
    }
}
