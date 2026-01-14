package dev.kylejulian.twsmanagement.configuration;

import dev.kylejulian.twsmanagement.util.LogUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MessageResolver {

    private static final String FILE_PREFIX = "@file:";
    private static final String FORMATS_FOLDER = "formats";

    private MessageResolver() {}

    public record ResolvedMessage(String viewer, String others, String fallback) {}

    public static ResolvedMessage resolve(JavaPlugin plugin, String raw) {
        if (raw == null || !raw.startsWith(FILE_PREFIX)) {
            return new ResolvedMessage(null, null, raw);
        }

        // Ensure formats directory exists
        Path formatsDir = plugin.getDataFolder().toPath().resolve(FORMATS_FOLDER);

        // Resolve file inside formats/
        String fileName = raw.substring(FILE_PREFIX.length()).trim();
        Path path = formatsDir.resolve(fileName);
        if (!Files.exists(path)) {
            LogUtils.error("Message file not found: " + path);
            return new ResolvedMessage(null, null, raw);
        }

        StringBuilder viewer = new StringBuilder();
        StringBuilder others = new StringBuilder();
        StringBuilder fallback = new StringBuilder();

        String mode = "fallback";

        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();

                if (trimmed.isEmpty()) {
                    append(mode, viewer, others, fallback, "");
                    continue;
                }

                if (trimmed.startsWith("#")) continue;

                if (trimmed.equalsIgnoreCase("Viewer:")) {
                    mode = "viewer";
                    continue;
                }

                if (trimmed.equalsIgnoreCase("Others:")) {
                    mode = "others";
                    continue;
                }

                append(mode, viewer, others, fallback, line);
            }
        } catch (IOException e) {
            LogUtils.error("Failed to read message file: " + path);
        }

        return new ResolvedMessage(
                emptyToNull(viewer),
                emptyToNull(others),
                emptyToNull(fallback)
        );
    }

    private static void append(String mode,
                               StringBuilder viewer,
                               StringBuilder others,
                               StringBuilder fallback,
                               String line) {
        switch (mode) {
            case "viewer" -> viewer.append(line).append('\n');
            case "others" -> others.append(line).append('\n');
            default -> fallback.append(line).append('\n');
        }
    }

    private static String emptyToNull(StringBuilder sb) {
        String s = sb.toString().trim();
        return s.isEmpty() ? null : s;
    }
}
