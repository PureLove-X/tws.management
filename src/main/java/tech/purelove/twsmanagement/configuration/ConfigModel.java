package tech.purelove.twsmanagement.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigModel {
	
	private AfkConfigModel afkConfig;
	private NightResetConfigModel nightResetConfig;
	private DatabaseConfigModel databaseConfig;
	private HudConfigModel hudConfig;
	private PortalConfigModel portalConfig;
	private AnnouncementConfigModel announcementConfig;

	public @Nullable AfkConfigModel getAfkConfig() { return this.afkConfig; }

	public @Nullable NightResetConfigModel getNightResetConfig() { return this.nightResetConfig; }
	
	public @Nullable DatabaseConfigModel getDatabaseConfig() {
		return this.databaseConfig;
	}

	public @Nullable HudConfigModel getHudConfig() { return this.hudConfig; }
	public PortalConfigModel getPortalConfig() {
		return portalConfig;
	}
	public AnnouncementConfigModel getAnnouncementConfig() {
		return announcementConfig;
	}
	public void setAfkConfig(@NotNull AfkConfigModel afkConfig) {
		this.afkConfig = afkConfig;
	}

	public void setNightResetConfig(@NotNull NightResetConfigModel nightResetConfig) {
		this.nightResetConfig = nightResetConfig;
	}
	
	public void setDatabaseConfig(@NotNull DatabaseConfigModel databaseConfig) {
		this.databaseConfig = databaseConfig;
	}

	public void setHudConfig(@NotNull HudConfigModel hudConfig) { this.hudConfig = hudConfig; }
	public void setPortalConfig(@NotNull PortalConfigModel portalConfig) { this.portalConfig = portalConfig; }
	public void setAnnouncementConfig(AnnouncementConfigModel announcementConfig) {
		this.announcementConfig = announcementConfig;
	}

}
