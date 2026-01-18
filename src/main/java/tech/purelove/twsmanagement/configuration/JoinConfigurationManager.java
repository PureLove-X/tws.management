package tech.purelove.twsmanagement.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import tech.purelove.twsmanagement.util.LogUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class JoinConfigurationManager {

    private JoinConfigModel config;
    private final File file;
    private final JavaPlugin plugin;

    public JoinConfigurationManager(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);
    }

    public void reload() {
        // Ensure plugin folder exists
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                LogUtils.error("Unable to create join config directory");
                return;
            }
        }

        // Ensure join.json exists
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
        }

        ensureBooksFolder();

        ObjectMapper mapper = new ObjectMapper();
        try {
            this.config = mapper.readValue(file, JoinConfigModel.class);
        } catch (IOException e) {
            LogUtils.error("Unable to read join config");
            LogUtils.error(e.getMessage());
            return;
        }

        boolean saveRequired = verifyDefaults(config);

        if (saveRequired) {
            try {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, config);
            } catch (IOException e) {
                LogUtils.error("Unable to write join config");
                LogUtils.error(e.getMessage());
            }
        }
    }

    private boolean verifyDefaults(@NotNull JoinConfigModel cfg) {
        boolean save = false;

        if (cfg.firstJoinMessage == null) {
            cfg.firstJoinMessage = new JoinConfigModel.FirstJoinMessage();
            save = true;
        }

        if (cfg.onJoinMessage == null) {
            cfg.onJoinMessage = new JoinConfigModel.OnJoinAnnouncement();
            save = true;
        }

        if (cfg.giveWrittenBooks == null) {
            cfg.giveWrittenBooks = new JoinConfigModel.GiveWrittenBooks();
            save = true;
        }

        if (cfg.teleport == null) {
            cfg.teleport = new JoinConfigModel.Teleport();
            save = true;
        }

        return save;
    }

    private void ensureBooksFolder() {
        File booksDir = new File(plugin.getDataFolder(), "books");

        if (!booksDir.exists() && !booksDir.mkdirs()) {
            LogUtils.error("Unable to create books directory");
            return;
        }

        File example = new File(booksDir, "Example.txt");
        if (!example.exists()) {
            try {
                plugin.saveResource("books/Example.txt", false);
            } catch (IllegalArgumentException e) {
                LogUtils.error("Missing default Example.txt in jar");
            }
        }
    }

    public JoinConfigModel getConfig() {
        return config;
    }
}
