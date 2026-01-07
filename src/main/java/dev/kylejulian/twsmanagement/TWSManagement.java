package dev.kylejulian.twsmanagement;

import dev.kylejulian.twsmanagement.afk.AfkEventListener;
import dev.kylejulian.twsmanagement.commands.AfkCommand;
import dev.kylejulian.twsmanagement.commands.HudCommand;
import dev.kylejulian.twsmanagement.commands.tabcompleters.AfkTabCompleter;
import dev.kylejulian.twsmanagement.configuration.*;
import dev.kylejulian.twsmanagement.data.DatabaseConnectionManager;
import dev.kylejulian.twsmanagement.data.MojangApi;
import dev.kylejulian.twsmanagement.data.interfaces.IDatabaseManager;
import dev.kylejulian.twsmanagement.data.interfaces.IExemptDatabaseManager;
import dev.kylejulian.twsmanagement.data.interfaces.IHudDatabaseManager;
import dev.kylejulian.twsmanagement.data.sqlite.AfkDatabaseManager;
import dev.kylejulian.twsmanagement.data.sqlite.HudDatabaseManager;
import dev.kylejulian.twsmanagement.player.PlayerListener;
import dev.kylejulian.twsmanagement.player.hud.HudListener;
import dev.kylejulian.twsmanagement.util.LogUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TWSManagement extends JavaPlugin {

    private final ConfigurationManager configManager;
    private DatabaseConnectionManager databaseConnectionManager;

	public TWSManagement() {
    	this.configManager = new ConfigurationManager(this, "config.json");
    }
	
	@Override
	public void onEnable() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		LogUtils.init(this);

		this.configManager.reload();
		LogUtils.info("Plugin configuration loaded in " + stopWatch.getTime() + "ms");

		ConfigModel config = this.configManager.getConfig();
		DatabaseConfigModel databaseConfig = config.getDatabaseConfig();
		AfkConfigModel afkConfig = config.getAfkConfig();
		NightResetConfigModel nightResetConfig = config.getNightResetConfig();
		HudConfigModel hudConfig = config.getHudConfig();

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

		this.getServer().getPluginManager().registerEvents(new PlayerListener(this, afkDatabaseManager, hudDatabaseManager, this.configManager), this);
		this.getServer().getPluginManager().registerEvents(new AfkEventListener(this, afkConfig), this);
		this.getServer().getPluginManager().registerEvents(new HudListener(this, hudConfig), this);

		LogUtils.info("Plugin Events have been registered by " + stopWatch.getTime() + "ms");

		MojangApi mojangApi = new MojangApi();
		Objects.requireNonNull(this.getCommand("afk"))
				.setExecutor(new AfkCommand(this, afkDatabaseManager, mojangApi));
		Objects.requireNonNull(this.getCommand("afk"))
				.setTabCompleter(new AfkTabCompleter(this));
		Objects.requireNonNull(this.getCommand("hud"))
				.setExecutor(new HudCommand(this, hudDatabaseManager));

		stopWatch.stop();
		LogUtils.success("Plugin started in " + stopWatch.getTime() + "ms");
	}

	@Override
	public void onDisable() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		this.databaseConnectionManager.closeConnections();
		LogUtils.info("All database connections have been closed in " + stopWatch.getTime() + "ms");
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

}
