package tech.purelove.twsmanagement.announcements;

import java.util.*;

public final class AnnouncementRegistry {

    private final Map<String, Announcement> announcements = new LinkedHashMap<>();

    public void clear() {
        announcements.clear();
    }

    public void register(Announcement announcement) {
        announcements.put(
                announcement.name().toLowerCase(Locale.ROOT),
                announcement
        );
    }

    public Announcement get(String name) {
        return announcements.get(name.toLowerCase(Locale.ROOT));
    }

    public Collection<Announcement> all() {
        return announcements.values();
    }

    public List<String> names() {
        return new ArrayList<>(announcements.keySet());
    }

    public List<Announcement> scheduled() {
        return announcements.values().stream()
                .filter(Announcement::scheduled)
                .toList();
    }
}
