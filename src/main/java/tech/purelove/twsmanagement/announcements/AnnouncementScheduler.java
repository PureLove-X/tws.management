package tech.purelove.twsmanagement.announcements;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tech.purelove.twsmanagement.extensions.TextProcessor;

import java.util.List;

public final class AnnouncementScheduler implements Runnable {

    private final AnnouncementRegistry registry;
    private int index = 0;

    public AnnouncementScheduler(AnnouncementRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void run() {
        List<Announcement> scheduled = registry.scheduled();
        if (scheduled.isEmpty()) return;

        Announcement announcement = scheduled.get(index);
        index = (index + 1) % scheduled.size();

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String line : announcement.lines()) {
                player.sendMessage(
                        TextProcessor.parse(line, player, player)
                );
            }
        }
    }
}
