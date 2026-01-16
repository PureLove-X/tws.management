package tech.purelove.twsmanagement;

import tech.purelove.twsmanagement.afk.AfkEventListener;
import tech.purelove.twsmanagement.announcements.AnnouncementLoader;
import tech.purelove.twsmanagement.announcements.AnnouncementModule;
import tech.purelove.twsmanagement.announcements.AnnouncementRegistry;
import tech.purelove.twsmanagement.commands.AfkCommand;
import tech.purelove.twsmanagement.commands.HudCommand;
import tech.purelove.twsmanagement.commands.JoinCommand;
import tech.purelove.twsmanagement.commands.PortalCommand;
import tech.purelove.twsmanagement.commands.tabcompleters.AfkTabCompleter;
import tech.purelove.twsmanagement.commands.tabcompleters.JoinTabCompleter;
import tech.purelove.twsmanagement.configuration.*;
import tech.purelove.twsmanagement.data.DatabaseConnectionManager;
import tech.purelove.twsmanagement.data.MojangApi;
import tech.purelove.twsmanagement.data.interfaces.IDatabaseManager;
import tech.purelove.twsmanagement.data.interfaces.IExemptDatabaseManager;
import tech.purelove.twsmanagement.data.interfaces.IHudDatabaseManager;
import tech.purelove.twsmanagement.data.sqlite.AfkDatabaseManager;
import tech.purelove.twsmanagement.data.sqlite.HudDatabaseManager;
import tech.purelove.twsmanagement.player.PlayerListener;
import tech.purelove.twsmanagement.player.hud.HudListener;
import tech.purelove.twsmanagement.util.LogUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TWSManagement extends JavaPlugin {

    private final ConfigurationManager configManager;
    private JoinConfigurationManager joinConfigManager;
	private DatabaseConnectionManager databaseConnectionManager;
	private AnnouncementRegistry announcementRegistry;
	private AnnouncementLoader announcementLoader;
	private AnnouncementModule announcementModule;

	public TWSManagement() {
    	this.configManager = new ConfigurationManager(this, "config.json");

    }
	
	@Override
	public void onEnable() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		LogUtils.init(this);

		this.configManager.reload();
		this.joinConfigManager = new JoinConfigurationManager(this, "join.json");
		this.joinConfigManager.reload();
		LogUtils.info("Plugin configuration loaded in " + stopWatch.getTime() + "ms");

		ConfigModel config = this.configManager.getConfig();
		DatabaseConfigModel databaseConfig = config.getDatabaseConfig();
		AfkConfigModel afkConfig = config.getAfkConfig();
		NightResetConfigModel nightResetConfig = config.getNightResetConfig();
		HudConfigModel hudConfig = config.getHudConfig();
		// onEnable()
		this.announcementRegistry = new AnnouncementRegistry();
		this.announcementLoader = new AnnouncementLoader(this, announcementRegistry);
		this.announcementModule =
				new AnnouncementModule(this, announcementRegistry, announcementLoader);

		announcementModule.enable();


		LogUtils.info(
				"Scheduled announcements loaded: " + announcementRegistry.scheduled().size()
		);

		if (databaseConfig == null || afkConfig == null || nightResetConfig == null ||
				hudConfig == null) {
			LogUtils.error("Failed start up. Unable to get configuration for plugin");
			return;
		}

		this.databaseConnectionManager = new DatabaseConnectionManager(databaseConfig, this.getDataFolder().getAbsolutePath());
		IExemptDatabaseManager afkDatabaseManager = new AfkDatabaseManager(this, this.databaseConnectionManager);
		IHudDatabaseManager hudDatabaseManager = new HudDatabaseManager(this, this.databaseConnectionManager);

		LogUtils.info("Internal dependencies have been created by " + stopWatch.getTime() + "ms");

		runDefaultSchemaSetup(new IDatabaseManager[] {afkDatabaseManager, hudDatabaseManager }, stopWatch);

		this.getServer().getPluginManager().registerEvents(new PlayerListener(this, afkDatabaseManager, hudDatabaseManager, this.configManager, this.joinConfigManager), this);
		this.getServer().getPluginManager().registerEvents(new AfkEventListener(this, afkConfig), this);
		this.getServer().getPluginManager().registerEvents(new HudListener(this, hudConfig), this);


		LogUtils.info("Plugin Events have been registered by " + stopWatch.getTime() + "ms");

		MojangApi mojangApi = new MojangApi();
		Objects.requireNonNull(this.getCommand("afk"))
				.setExecutor(new AfkCommand(this, afkDatabaseManager, mojangApi));
		Objects.requireNonNull(this.getCommand("afk"))
				.setTabCompleter(new AfkTabCompleter(this));
		Objects.requireNonNull(this.getCommand("hud"))
				.setExecutor(new HudCommand(this, hudDatabaseManager, configManager));
		Objects.requireNonNull(this.getCommand("tws"))
				.setExecutor(new JoinCommand(this));
		Objects.requireNonNull(this.getCommand("tws"))
				.setTabCompleter(new JoinTabCompleter(this));
		Objects.requireNonNull(this.getCommand("portal")).setExecutor(new PortalCommand(this, configManager));

		stopWatch.stop();
		LogUtils.success("Plugin started in " + stopWatch.getTime() + "ms");
	}

	@Override
	public void onDisable() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		if (this.databaseConnectionManager != null) {
			this.databaseConnectionManager.closeConnections();
			LogUtils.info("All database connections have been closed in " + stopWatch.getTime() + "ms");
		}

		stopWatch.stop();
		LogUtils.info("Plugin stopped in " + stopWatch.getTime() + "ms");
	}


	private void runDefaultSchemaSetup(
			@NotNull IDatabaseManager[] databaseManagers,
			StopWatch stopWatch
	) {
		for (IDatabaseManager databaseManager : databaseManagers) {
			try {
				databaseManager.setupDefaultSchema().get();

				LogUtils.info(
						databaseManager.getClass().getSimpleName()
								+ " verified in "
								+ stopWatch.getTime()
								+ "ms"
				);

			} catch (InterruptedException | ExecutionException e) {

				LogUtils.error(
						"Unable to setup database schemas. Plugin may not work as expected. Disabling plugin."
				);

				this.getServer().getPluginManager().disablePlugin(this);
				return;
			}
		}
	}
	public JoinConfigurationManager getJoinConfigManager() {
		return joinConfigManager;
	}
	public ConfigurationManager getConfigurationManager() {return this.configManager;}
	public void reloadAnnouncements() {
		if (announcementModule != null) {
			announcementModule.reload();
		}
	}


}
