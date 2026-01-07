package dev.kylejulian.twsmanagement.commands;

import dev.kylejulian.twsmanagement.data.interfaces.IHudDatabaseManager;
import dev.kylejulian.twsmanagement.player.hud.events.HudEvent;
import dev.kylejulian.twsmanagement.util.LogUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record HudCommand(JavaPlugin plugin,
                         IHudDatabaseManager hudDatabaseManager) implements CommandExecutor {

    public HudCommand(@NotNull final JavaPlugin plugin, @NotNull final IHudDatabaseManager hudDatabaseManager) {
        this.plugin = plugin;
        this.hudDatabaseManager = hudDatabaseManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (sender instanceof Player player) {
            final UUID playerId = player.getUniqueId();

            CompletableFuture<Boolean> playerHudIsEnabledFuture = hudDatabaseManager.isEnabled(playerId);
            playerHudIsEnabledFuture.thenAcceptAsync(result -> {
                if (!result) {
                    CompletableFuture<Void> enablePlayerHudFuture = hudDatabaseManager.addPlayer(playerId);
                    enablePlayerHudFuture.join();

                    raiseHudEvent(playerId, true);
                } else {
                    CompletableFuture<Void> disablePlayerHudFuture = hudDatabaseManager.removePlayer(playerId);
                    disablePlayerHudFuture.join();

                    raiseHudEvent(playerId, false);
                }
            });

            return true;
        }
        LogUtils.warn("You must be a player to use this command!");
        return false;
    }

    /**
     * Raises an Event to trigger Hud event service
     *
     * @param playerId Player who triggered the event
     * @param enabled  To indicate if a player wants the Hud to be visible or not
     */
    private void raiseHudEvent(@NotNull final UUID playerId, final boolean enabled) {
        Runnable runnable = () -> plugin.getServer().getPluginManager().callEvent(new HudEvent(playerId, enabled));
        this.plugin.getServer().getScheduler().runTask(this.plugin, runnable);
    }
}
