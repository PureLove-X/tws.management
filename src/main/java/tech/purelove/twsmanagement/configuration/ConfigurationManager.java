package tech.purelove.twsmanagement.configuration;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import tech.purelove.twsmanagement.util.LogUtils;
import org.bukkit.plugin.java.JavaPlugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

public class ConfigurationManager {

    private ConfigModel config;
    private final File file;
    private final JavaPlugin plugin;

    private final String[] defaultAfkEvents = new String[]{"onPlayerChat",
            "onPlayerInteractEntity",
            "onPlayerInteract",
            "onPlayerBedEnter",
            "onPlayerChangedWorld",
            "onPlayerEditBook",
            "onPlayerDropItem",
            "onPlayerItemBreak",
            "onPlayerShearEntity",
            "onPlayerToggleFlight",
            "onPlayerToggleSprint",
            "onPlayerToggleSneak",
            "onPlayerUnleashEntity",
            "onPlayerBucketFill",
            "onPlayerBucketEmpty",
            "onPlayerMove",
            "onPlayerExpChange"};

    public ConfigurationManager(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(this.plugin.getDataFolder(), fileName);
    }

    /**
     * Reloads the Configuration File
     */
    public void reload() {
        if (!this.file.getParentFile().exists()) {
            boolean makeDirectoryResult = this.file.getParentFile().mkdirs();
            if (!makeDirectoryResult) {
                LogUtils.error("Unable to make directory");
                return;
            }
        }

        if (!this.file.exists()) {
            this.plugin.saveResource(this.file.getName(), false);
        }

        ensureFormatsFolder();

        if (!this.file.exists()) {
            this.plugin.saveResource(this.file.getName(), false);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            this.config = mapper.readValue(this.file, ConfigModel.class);
        } catch (IOException e) {
            LogUtils.error("Unable to read configuration file");
            LogUtils.error(e.getMessage());
        }

        boolean saveRequired = verifyConfigurationDefaults(this.config);

        if (saveRequired) {
            try {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(this.file, this.config);
            } catch (IOException e) {
                LogUtils.error("Unable to write to configuration file");
                LogUtils.error(e.getMessage());
            }
        }
    }

    private boolean verifyConfigurationDefaults(@NotNull ConfigModel configModel) {
        boolean saveRequired = false;

        // Set default Afk configuration
        if (configModel.getAfkConfig() == null) {
            AfkConfigModel afkConfigModel = new AfkConfigModel();
            afkConfigModel.setEvents(this.defaultAfkEvents);
            afkConfigModel.setKickMessage("You have been kicked for being AFK for too long!");
            afkConfigModel.setKickTimeMinutes(5);
            afkConfigModel.setPlayerCountNeededForKick(25);
            afkConfigModel.setTimeMinutes(5);
            afkConfigModel.setSendPlayerAfkMessage(true);
            afkConfigModel.setAfkKick(true);

            configModel.setAfkConfig(afkConfigModel);
            saveRequired = true;
        }

        // Set default Night Reset configuration
        if (configModel.getNightResetConfig() == null) {
            NightResetConfigModel nightResetConfigModel = new NightResetConfigModel();
            nightResetConfigModel.setEnabled(true);

            configModel.setNightResetConfig(nightResetConfigModel);
            saveRequired = true;
        }

        // Set default Hud configuration
        if (configModel.getHudConfig() == null) {
            HudConfigModel hudConfigModel = new HudConfigModel();
            hudConfigModel.setEnabled(true);
            hudConfigModel.setRefreshRateTicks(20); // Every second
            hudConfigModel.setAutoEnableOnJoin(true);
            configModel.setHudConfig(hudConfigModel);
            saveRequired = true;
        }
        // Patch existing Hud configuration (non-destructive)
        HudConfigModel hudConfig = configModel.getHudConfig();
        if (hudConfig != null) {
            if (!hudConfig.hasAutoEnableOnJoin()) {
                hudConfig.setAutoEnableOnJoin(true);
                saveRequired = true;
            }
        }
        // Set default Portal configuration
        if (configModel.getPortalConfig() == null) {
            PortalConfigModel portal = new PortalConfigModel();
            portal.setEnabled(true);
            portal.setNetherY_Level(127);
            portal.setAvoidHubRadius(100);
            portal.setAllowNetherToOverworld(true);
            portal.setMessageNether("@file:portal_nether.txt");
            portal.setMessageOverworld("@file:portal_overworld.txt");

            configModel.setPortalConfig(portal);
            saveRequired = true;
        }
        // Set default Announcement configuration
        if (configModel.getAnnouncementConfig() == null) {
            AnnouncementConfigModel announcement = new AnnouncementConfigModel();
            announcement.setEnabled(true);
            announcement.setIntervalSeconds(1800);
            announcement.setMessages("@file:announcements.txt");

            configModel.setAnnouncementConfig(announcement);
            saveRequired = true;
        }
        // Set default Database configuration
        if (configModel.getDatabaseConfig() == null) {
            DatabaseConfigModel databaseConfigModel = new DatabaseConfigModel();
            databaseConfigModel.setName("tws-local.db");
            databaseConfigModel.setMaxConcurrentConnections(5);

            configModel.setDatabaseConfig(databaseConfigModel);
            saveRequired = true;
        }

        return saveRequired;
    }
    private void ensureFormatsFolder() {
        File formatsDir = new File(plugin.getDataFolder(), "formats");

        if (!formatsDir.exists() && !formatsDir.mkdirs()) {
            LogUtils.error("Unable to create formats directory");
            return;
        }

        ensureFormatFile(formatsDir, "join_msg.txt");
        ensureFormatFile(formatsDir, "portal_nether.txt");
        ensureFormatFile(formatsDir, "portal_overworld.txt");
        ensureFormatFile(formatsDir, "announcements.txt");
    }

    private void ensureFormatFile(File formatsDir, String fileName) {
        File target = new File(formatsDir, fileName);

        if (!target.exists()) {
            try {
                plugin.saveResource("formats/" + fileName, false);
            } catch (IllegalArgumentException e) {
                LogUtils.error("Missing default format file in jar: " + fileName);
            }
        }
    }

    /**
     * Get the current Configuration
     *
     * @return Configuration
     */
    public ConfigModel getConfig() {
        return this.config;
    }
}
