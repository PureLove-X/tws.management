package dev.kylejulian.twsmanagement.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.kylejulian.twsmanagement.util.LogUtils;
import org.bukkit.plugin.java.JavaPlugin;

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
            file.getParentFile().mkdirs();
        }

        // Ensure join.json exists
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
        }

        // Ensure books folder + example.txt exist
        ensureBooksFolder();

        ObjectMapper mapper = new ObjectMapper();
        try {
            this.config = mapper.readValue(file, JoinConfigModel.class);
        } catch (IOException e) {
            LogUtils.error("Unable to read join config");
            LogUtils.error(e.getMessage());
        }

        boolean saveRequired = verifyDefaults();

        if (saveRequired) {
            try {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, config);
            } catch (IOException e) {
                LogUtils.error("Unable to write join config");
            }
        }
    }

    private void ensureBooksFolder() {
        File booksDir = new File(plugin.getDataFolder(), "books");
        if (!booksDir.exists()) {
            booksDir.mkdirs();
        }

        File example = new File(booksDir, "Example.txt");
        if (!example.exists()) {
            plugin.saveResource("books/Example.txt", false);
        }
    }

    private boolean verifyDefaults() {
        boolean save = false;

        if (config == null) {
            config = new JoinConfigModel();
            return true;
        }

        if (config.firstJoinMessage == null) {
            config.firstJoinMessage = new JoinConfigModel.FirstJoinMessage();
            save = true;
        }

        if (config.giveWrittenBooks == null) {
            config.giveWrittenBooks = new JoinConfigModel.GiveWrittenBooks();
            save = true;
        }

        if (config.teleport == null) {
            config.teleport = new JoinConfigModel.Teleport();
            save = true;
        }

        return save;
    }

    public JoinConfigModel getConfig() {
        return config;
    }
}
