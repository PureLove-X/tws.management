package dev.kylejulian.twsmanagement.player.hud;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HudDisplayRunnable extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final UUID playerId;

    public HudDisplayRunnable(@NotNull JavaPlugin plugin, @NotNull UUID playerId) {
        this.playerId = playerId;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        final Player player = this.plugin.getServer().getPlayer(this.playerId);
        if (player == null) {
            return;
        }

        final Location location = player.getLocation();
        final double x = location.getX();
        final double y = location.getY();
        final double z = location.getZ();

        final long playerWorldTime = player.getWorld().getTime();
        final float playerYaw = location.getYaw();

        String xDisplay = "§a§lX §r%.1f ";
        final Component xFormatted = Component.text(String.format(xDisplay, x));
        String yDisplay = "§a§lY §r%.1f ";
        final Component yFormatted = Component.text(String.format(yDisplay, y));
        String zDisplay = "§a§lZ §r%.1f ";
        final Component zFormatted = Component.text(String.format(zDisplay, z));
        String orientationDisplay = "§e§l%s ";
        final Component orientationFormatted = Component.text(String.format(orientationDisplay, Orientation.getOrientation(playerYaw)));
        String timeDisplay = "§c§l%s";
        final Component timeFormatted = Component.text(String.format(timeDisplay, Time.ticksToTime(playerWorldTime)));

        Component baseComponent = Component.empty()
            .append(xFormatted)
            .append(yFormatted)
            .append(zFormatted)
            .append(orientationFormatted)
            .append(timeFormatted);

        player.sendActionBar(baseComponent);
    }
}
