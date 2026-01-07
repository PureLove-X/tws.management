package dev.kylejulian.twsmanagement.afk;

import dev.kylejulian.twsmanagement.afk.events.AfkEvent;
import dev.kylejulian.twsmanagement.configuration.AfkConfigModel;
import dev.kylejulian.twsmanagement.data.interfaces.IExemptDatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 
 * Manages the AFK for a given player
 *
 */
public class AfkManager implements Runnable {

	private int afkMinutes;
	private final boolean alreadyAfk;
	private final JavaPlugin plugin;
	private final IExemptDatabaseManager afkDatabase;
	private final AfkConfigModel afkConfig;
	private final UUID playerId;

	/**
	 * Creates a new AfkManager for a given Player
	 * @param plugin JavaPlugin
	 * @param afkDatabase AFK Database Manager
	 * @param afkConfig Configuration for AFK
	 * @param playerId Player whom this AFK Manager belongs to
	 * @param alreadyAfk Boolean flag to indicate whether or not the Player was already AFK when this A
	 *                      FK Manager was created
	 */
	public AfkManager(JavaPlugin plugin, IExemptDatabaseManager afkDatabase, AfkConfigModel afkConfig, UUID playerId,
					  boolean alreadyAfk) {
		this.plugin = plugin;
		this.afkDatabase = afkDatabase;
		this.afkConfig = afkConfig;
		this.playerId = playerId;
		this.alreadyAfk = alreadyAfk;
	}
	
	@Override
	public void run() {
		int afkTime = this.afkConfig.getTimeMinutes();
		int afkKickTime = this.afkConfig.getKickTimeMinutes();
		
		Player player = this.plugin.getServer().getPlayer(this.playerId);
		if (player == null) { // Player does not exist? Should not be possible since this Runnable would be canceled
			return;
		}
		
		this.addPlayerAfkMinutes();
		
		if (!this.alreadyAfk && this.getPlayerAfkMinutes() == afkTime) { // Player is AFK
			AfkEvent event = new AfkEvent(this.playerId);
			
			this.plugin.getServer().getScheduler()
					.runTask(this.plugin, () -> this.plugin.getServer().getPluginManager().callEvent(event));
		}

		CompletableFuture<Boolean> isPlayerAfkKickExemptFuture = this.afkDatabase.isExempt(playerId);
		isPlayerAfkKickExemptFuture.thenAcceptAsync(result -> {
			if (!result) { // Player is not Kick exempt
				if (canKickPlayer(afkKickTime, afkTime)) {
					// Player must be kicked synchronously
					plugin.getServer().getScheduler()
							.runTask(this.plugin, () -> player.kick(getKickMessageComponent(afkConfig)));
				}
			}
		});
	}
	
	private boolean canKickPlayer(int afkKickTime, int afkTime) {
		// Player has to be inactive for both the AFK time and AFK Kick time
		if (this.getPlayerAfkMinutes() >= (afkKickTime + afkTime)) {
			// Only kick if it is enabled in the configuration
			if (this.afkConfig.getAfkKick()) {
				int numberOfPlayersOnline = this.plugin.getServer().getOnlinePlayers().size(); 
				int numberOfRequiredPlayersToKick = this.afkConfig.getPlayerCountNeededForKick();
	
				return numberOfPlayersOnline >= numberOfRequiredPlayersToKick;
			}
		}
		
		return false;
	}

	private int getPlayerAfkMinutes() {
		return this.afkMinutes;
	}
	
	private void addPlayerAfkMinutes() {
		this.afkMinutes = this.afkMinutes + 1;
	}

	private TextComponent getKickMessageComponent(AfkConfigModel afkConfig) {
		String message = afkConfig.getKickMessage();
		return Component.text(message);
	}
}
