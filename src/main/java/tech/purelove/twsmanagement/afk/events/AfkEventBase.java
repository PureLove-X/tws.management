package tech.purelove.twsmanagement.afk.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

abstract class AfkEventBase extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	private final UUID playerId;
	
	public AfkEventBase(UUID playerId) {
		this.playerId = playerId;
	}

	public @NotNull UUID getPlayerId() {
		return this.playerId;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
	
	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}