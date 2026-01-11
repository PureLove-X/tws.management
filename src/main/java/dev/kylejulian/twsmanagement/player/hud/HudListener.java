package dev.kylejulian.twsmanagement.player.hud;

import dev.kylejulian.twsmanagement.configuration.HudConfigModel;
import dev.kylejulian.twsmanagement.player.hud.events.HudEvent;
import dev.kylejulian.twsmanagement.util.LogUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class HudListener implements Listener {

    private final JavaPlugin plugin;
    private final HashMap<UUID, Integer> playerTask;
    private final HudConfigModel hudConfig;

    public HudListener(@NotNull JavaPlugin plugin, @NotNull HudConfigModel hudConfig) {
        this.plugin = plugin;
        this.playerTask = new HashMap<>();
        this.hudConfig = hudConfig;
    }

    @EventHandler
    public void onLeave(@NotNull PlayerQuitEvent e) {
        final UUID playerId = e.getPlayer().getUniqueId();
        if (this.playerTask.containsKey(playerId)) {
            Integer taskId = this.playerTask.get(playerId);
            this.plugin.getServer().getScheduler().cancelTask(taskId);
            this.playerTask.remove(playerId);
        }
    }

    @EventHandler
    public void onHudRaised(@NotNull final HudEvent e) {
        if (!this.hudConfig.getEnabled()) {
            return;
        }

        final UUID playerId = e.getPlayerId();
        if (e.getEnabled()) { // Player wants task enabled
            if (!this.playerTask.containsKey(playerId)) {
                Integer refreshRateTicks = this.hudConfig.getRefreshRateTicks();
                if (refreshRateTicks == null || refreshRateTicks < 1) {
                    LogUtils.warn("Invalid configuration for Hud Refresh Rate. Setting Refresh Rate to 10 ticks.");
                    refreshRateTicks = 10;
                }

                final BukkitTask runnable = new HudDisplayRunnable(this.plugin, playerId)
                        .runTaskTimerAsynchronously(this.plugin, 0, refreshRateTicks); // 10 ticks = 0.5 seconds

                final Integer taskId = runnable.getTaskId();
                this.playerTask.put(playerId, taskId);
            }
        }
        else {
            if (this.playerTask.containsKey(playerId)) {
                Integer taskId = this.playerTask.get(playerId);
                this.plugin.getServer().getScheduler().cancelTask(taskId);
                this.playerTask.remove(playerId);
            }

            // Clear the action bar immediately (prevents the fade-out lingering)
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                Player player = this.plugin.getServer().getPlayer(playerId);
                if (player != null) {
                    player.sendActionBar(Component.empty());
                }
            });
        }

    }
}
