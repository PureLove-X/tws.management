package dev.kylejulian.twsmanagement.commands;

import dev.kylejulian.twsmanagement.configuration.ConfigurationManager;
import dev.kylejulian.twsmanagement.configuration.HudConfigModel;
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

public record HudCommand(
        JavaPlugin plugin,
        IHudDatabaseManager hudDatabaseManager,
        ConfigurationManager configManager
) implements CommandExecutor {

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) {
            LogUtils.warn("You must be a player to use this command!");
            return false;
        }

        final UUID playerId = player.getUniqueId();

        final HudConfigModel hudConfig = configManager.getConfig().getHudConfig();
        if (hudConfig == null || !hudConfig.getEnabled()) {
            return true; // Hud feature disabled server-wide
        }

        final boolean autoEnableOnJoin = Boolean.TRUE.equals(hudConfig.getAutoEnableOnJoin());

        CompletableFuture<Boolean> hasPreferenceFuture = hudDatabaseManager.isEnabled(playerId);
        hasPreferenceFuture.thenAcceptAsync(hasPreference -> {

            // Toggle preference in DB
            final boolean newHasPreference = !hasPreference;

            if (newHasPreference) {
                hudDatabaseManager.addPlayer(playerId).join();
            } else {
                hudDatabaseManager.removePlayer(playerId).join();
            }

            // Interpret preference the same way as onJoin
            final boolean shouldEnableHud = computeShouldEnableHud(autoEnableOnJoin, newHasPreference);

            raiseHudEvent(playerId, shouldEnableHud);
        });

        return true;
    }

    /**
     * Same truth table as PlayerListener.onJoin:
     * autoEnableOnJoin = true  -> default ON, preference = opt-out  -> enabled = !hasPreference
     * autoEnableOnJoin = false -> default OFF, preference = opt-in  -> enabled = hasPreference
     */
    private static boolean computeShouldEnableHud(boolean autoEnableOnJoin, boolean hasPreference) {
        return autoEnableOnJoin ? !hasPreference : hasPreference;
    }

    private void raiseHudEvent(@NotNull final UUID playerId, final boolean enabled) {
        Runnable runnable = () -> plugin.getServer().getPluginManager().callEvent(new HudEvent(playerId, enabled));
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }
}
