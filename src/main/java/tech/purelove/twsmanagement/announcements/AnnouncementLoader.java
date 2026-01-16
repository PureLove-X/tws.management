package tech.purelove.twsmanagement.announcements;

import org.bukkit.plugin.java.JavaPlugin;
import tech.purelove.twsmanagement.configuration.AnnouncementConfigModel;
import tech.purelove.twsmanagement.configuration.MessageResolver;
import tech.purelove.twsmanagement.util.LogUtils;

import java.util.List;
import java.util.Map;

public final class AnnouncementLoader {

    private final JavaPlugin plugin;
    private final AnnouncementRegistry registry;

    public AnnouncementLoader(JavaPlugin plugin, AnnouncementRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public void load(AnnouncementConfigModel config) {
        registry.clear();

        if (config == null || !config.isEnabled()) {
            LogUtils.info("Announcements are disabled");
            return;
        }

        Object source = config.getMessages();
        if (source == null) {
            LogUtils.warn("No announcement messages configured");
            return;
        }

        String content = resolveSource(source); // ← this is what you’re missing
        if (content == null || content.isBlank()) {
            LogUtils.warn("Announcement content is empty");
            return;
        }

        AnnouncementParser.parse(content, registry);
    }
    @SuppressWarnings("unchecked")
    private String resolveSource(Object source) {

        // @file: announcements
        if (source instanceof String s) {
            MessageResolver.ResolvedMessage resolved =
                    MessageResolver.resolve(plugin, s);
            return resolved.fallback();
        }

        // Inline JSON announcements
        if (source instanceof Map<?, ?> root) {
            StringBuilder out = new StringBuilder();

            Map<String, Object> map = (Map<String, Object>) root;

            Object scheduled = map.get("scheduled");
            if (scheduled instanceof Map<?, ?> schedMap) {
                out.append("@Schedule\n");
                appendSection(out, (Map<String, Object>) schedMap);
            }

            Object broadcast = map.get("broadcast");
            if (broadcast instanceof Map<?, ?> bcMap) {
                out.append("@Broadcast\n");
                appendSection(out, (Map<String, Object>) bcMap);
            }

            return out.toString();
        }

        LogUtils.warn("Unsupported announcement message format");
        return null;
    }
    private void appendSection(
            StringBuilder out,
            Map<String, Object> section
    ) {
        for (var entry : section.entrySet()) {
            out.append('[').append(entry.getKey()).append("]\n");

            if (entry.getValue() instanceof List<?> lines) {
                for (Object line : lines) {
                    out.append(line.toString()).append('\n');
                }
            }
        }
    }


}
