package tech.purelove.twsmanagement.announcements;

import org.bukkit.plugin.java.JavaPlugin;
import tech.purelove.twsmanagement.configuration.AnnouncementConfigModel;
import tech.purelove.twsmanagement.configuration.MessageResolver;
import tech.purelove.twsmanagement.util.LogUtils;

public final class AnnouncementLoader {

    private final JavaPlugin plugin;
    private final AnnouncementRegistry registry;

    public AnnouncementLoader(JavaPlugin plugin, AnnouncementRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public void load(AnnouncementConfigModel config) {
        registry.clear();

        if (config == null || !config.isEnabled()) return;

        Object source = config.getMessages();
        if (!(source instanceof String s)) {
            LogUtils.warn("Inline announcements not supported yet");
            return;
        }

        String content = MessageResolver.resolve(plugin, s).fallback();
        if (content == null || content.isBlank()) return;

        AnnouncementParser.parse(content, registry);

        LogUtils.info("Loaded " + registry.all().size() + " announcements");
    }
}
