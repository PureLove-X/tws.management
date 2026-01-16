package tech.purelove.twsmanagement.player.hud.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HudEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID playerId;
    private final boolean enabled;

    public HudEvent(@NotNull UUID playerId, boolean enabled) {
        this.playerId = playerId;
        this.enabled = enabled;
    }

    public @NotNull UUID getPlayerId() {
        return this.playerId;
    }

    public boolean getEnabled() { return this.enabled; }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
