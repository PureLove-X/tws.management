package tech.purelove.twsmanagement.announcements;

import org.bukkit.Bukkit;
import tech.purelove.twsmanagement.TWSManagement;
import tech.purelove.twsmanagement.announcements.commands.BroadcastCommand;
import tech.purelove.twsmanagement.configuration.AnnouncementConfigModel;
import tech.purelove.twsmanagement.util.LogUtils;

import java.util.Objects;

public final class AnnouncementModule {

    private final TWSManagement plugin;
    private final AnnouncementRegistry registry;
    private final AnnouncementLoader loader;

    private int taskId = -1;

    public AnnouncementModule(
            TWSManagement plugin,
            AnnouncementRegistry registry,
            AnnouncementLoader loader
    ) {
        this.plugin = plugin;
        this.registry = registry;
        this.loader = loader;
    }

    public void enable() {
        reload();

        BroadcastCommand command = new BroadcastCommand(registry);
        Objects.requireNonNull(plugin.getCommand("broadcast"))
                .setExecutor(command);
        Objects.requireNonNull(plugin.getCommand("broadcast"))
                .setTabCompleter(command);
    }

    public void reload() {
        stop();

        AnnouncementConfigModel cfg =
                plugin.getConfigurationManager()
                        .getConfig()
                        .getAnnouncementConfig();

        if (cfg == null || !cfg.isEnabled()) return;

        loader.load(cfg);

        int intervalSeconds = cfg.getIntervalSeconds();
        int intervalTicks = intervalSeconds * 20;
        int initialDelayTicks = 60 * 20; // 1 minute

        LogUtils.info(
                "Announcement scheduler starting in 60s, repeating every "
                        + intervalTicks + " ticks"
        );

        AnnouncementScheduler scheduler = new AnnouncementScheduler(registry);

        taskId = Bukkit.getScheduler().runTaskTimer(
                plugin,
                scheduler,
                initialDelayTicks,
                intervalTicks
        ).getTaskId();
    }



    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}
