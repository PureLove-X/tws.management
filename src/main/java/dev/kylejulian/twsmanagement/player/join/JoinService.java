package dev.kylejulian.twsmanagement.player.join;

import dev.kylejulian.twsmanagement.configuration.JoinConfigModel;
import dev.kylejulian.twsmanagement.configuration.MessageResolver;
import dev.kylejulian.twsmanagement.extensions.TextProcessor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class JoinService {

    private JoinService() {}

    public static void runFirstJoin(
            JavaPlugin plugin,
            Player player,
            JoinConfigModel cfg
    ) {
        // Welcome message (broadcast, viewerMode allowed)
        if (cfg.firstJoinMessage.enabled) {
            MessageResolver.ResolvedMessage msg =
                    MessageResolver.resolve(plugin, cfg.firstJoinMessage.message);

            for (Player viewer : plugin.getServer().getOnlinePlayers()) {
                String raw;

                if (viewer.equals(player) && msg.viewer() != null) {
                    raw = msg.viewer();
                } else if (!viewer.equals(player) && msg.others() != null) {
                    raw = msg.others();
                } else {
                    raw = msg.fallback();
                }

                if (raw == null || raw.isEmpty()) continue;

                viewer.sendMessage(
                        TextProcessor.parse(raw, viewer, player)
                );
            }

        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

            if (!player.isOnline()) return;

            // Teleport
            if (cfg.teleport.enabled) {
                World world = Bukkit.getWorld(cfg.teleport.world);
                if (world != null) {
                    player.teleport(new Location(
                            world,
                            cfg.teleport.x + 0.5,
                            cfg.teleport.y,
                            cfg.teleport.z + 0.5,
                            cfg.teleport.yaw,
                            cfg.teleport.pitch
                    ));
                }
            }

            // Books
            if (cfg.giveWrittenBooks.enabled) {
                for (String fileName : cfg.giveWrittenBooks.bookFiles) {
                    File file = new File(plugin.getDataFolder(), "books/" + fileName);
                    ItemStack book = BookLoader.load(file, player);
                    if (book != null) {
                        player.getInventory().addItem(book);
                    }
                }
            }

        }, cfg.delay * 20L);
    }
}
