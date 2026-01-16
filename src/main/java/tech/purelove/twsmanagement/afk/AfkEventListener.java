package tech.purelove.twsmanagement.afk;

import tech.purelove.twsmanagement.afk.events.AfkCancelledEvent;
import tech.purelove.twsmanagement.configuration.AfkConfigModel;
import io.papermc.paper.event.player.AsyncChatEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Listener for Player Events, used to send the AFK Canceled events of Players
 */
public record AfkEventListener(JavaPlugin plugin,
							   AfkConfigModel afkConfig) implements Listener {

	private void raiseAfkCancelledEvent(Player player) {
		AfkCancelledEvent event = new AfkCancelledEvent(player.getUniqueId());

		this.plugin.getServer().getScheduler()
				.runTask(this.plugin, () -> this.plugin.getServer().getPluginManager().callEvent(event));
	}

	private boolean configContainsPlayerEvent(String eventName) {
		if (this.afkConfig != null && this.afkConfig.getEvents() != null) {
			for (String event : this.afkConfig.getEvents()) {
				if (event.equalsIgnoreCase(eventName)) {
					return true;
				}
			}
		}

		return false;
	}

	@EventHandler
	public void onPlayerChat(AsyncChatEvent e) {
		if (this.configContainsPlayerEvent("onPlayerChat")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if (this.configContainsPlayerEvent("onPlayerInteractEntity")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (this.configContainsPlayerEvent("onPlayerInteract")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerBedEnter(PlayerBedEnterEvent e) {
		if (this.configContainsPlayerEvent("onPlayerBedEnter")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
		if (this.configContainsPlayerEvent("onPlayerChangedWorld")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerEditBook(PlayerEditBookEvent e) {
		if (this.configContainsPlayerEvent("onPlayerEditBook")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (this.configContainsPlayerEvent("onPlayerDropItem")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerItemBreak(PlayerItemBreakEvent e) {
		if (this.configContainsPlayerEvent("onPlayerItemBreak")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerShearEntity(PlayerShearEntityEvent e) {
		if (this.configContainsPlayerEvent("onPlayerShearEntity")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
		if (this.configContainsPlayerEvent("onPlayerToggleFlight")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerToggleSprint(PlayerToggleSprintEvent e) {
		if (this.configContainsPlayerEvent("onPlayerToggleSprint")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
		if (this.configContainsPlayerEvent("onPlayerToggleSneak")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerUnleashEntity(PlayerUnleashEntityEvent e) {
		if (this.configContainsPlayerEvent("onPlayerUnleashEntity")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent e) {
		if (this.configContainsPlayerEvent("onPlayerBucketFill")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
		if (this.configContainsPlayerEvent("onPlayerBucketEmpty")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (this.configContainsPlayerEvent("onPlayerMove")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerExpChange(PlayerExpChangeEvent e) {
		if (this.configContainsPlayerEvent("onPlayerExpChange")) {
			this.raiseAfkCancelledEvent(e.getPlayer());
		}
	}
}
