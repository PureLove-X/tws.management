package tech.purelove.twsmanagement.commands;

import tech.purelove.twsmanagement.configuration.ConfigurationManager;
import tech.purelove.twsmanagement.configuration.MessageResolver;
import tech.purelove.twsmanagement.configuration.PortalConfigModel;
import tech.purelove.twsmanagement.extensions.TextProcessor;
import tech.purelove.twsmanagement.player.portal.PortalCalculator;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class PortalCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;

    public PortalCommand(JavaPlugin plugin, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        PortalConfigModel cfg = configManager.getConfig().getPortalConfig();
        if (cfg == null || !cfg.getEnabled()) {
            return true;
        }

        boolean inNether = player.getWorld().getEnvironment() == World.Environment.NETHER;

        // Block Nether → Overworld if disabled
        if (inNether && !cfg.getAllowNetherToOverworld()) {
            return true;
        }

        String raw;
        Map<String, String> placeholders = new HashMap<>();

        if (!inNether) {
            // =========================
            // Overworld → Nether
            // =========================
            PortalCalculator result = PortalCalculator.calculate(
                    player.getLocation().getX(),
                    player.getLocation().getZ(),
                    cfg.getAvoidHubRadius()
            );

            MessageResolver.ResolvedMessage resolved =
                    MessageResolver.resolve(plugin, cfg.getMessageNether());

            raw = resolved.viewer() != null
                    ? resolved.viewer()
                    : resolved.fallback();

            placeholders.put("Nether_X", String.valueOf(result.netherX));
            placeholders.put("Nether_Y", String.valueOf(cfg.getNetherY_Level()));
            placeholders.put("Nether_Z", String.valueOf(result.netherZ));
            placeholders.put("Nether_Tunnel", result.tunnel);
            placeholders.put("Nether_Facing", result.facing);
            placeholders.put("Nether_Code", result.tunnelCode);

        } else {
            // =========================
            // Nether → Overworld
            // =========================
            PortalCalculator.OverworldResult ow =
                    PortalCalculator.calculateOverworld(
                            player.getLocation().getX(),
                            player.getLocation().getZ()
                    );

            MessageResolver.ResolvedMessage resolved =
                    MessageResolver.resolve(plugin, cfg.getMessageOverworld());

            raw = resolved.viewer() != null
                    ? resolved.viewer()
                    : resolved.fallback();

            placeholders.put("Overworld_X", String.valueOf(ow.overworldX));
            placeholders.put("Overworld_Y", String.valueOf(player.getLocation().getBlockY()));
            placeholders.put("Overworld_Z", String.valueOf(ow.overworldZ));
        }

        if (raw == null || raw.isEmpty()) {
            return true;
        }

        player.sendMessage(
                TextProcessor.parse(
                        raw,
                        player, // viewer
                        player, // sender
                        placeholders
                )
        );

        return true;
    }
}
