package tech.purelove.twsmanagement.commands;

import tech.purelove.twsmanagement.TWSManagement;
import tech.purelove.twsmanagement.configuration.JoinConfigModel;
import tech.purelove.twsmanagement.player.join.JoinService;
import tech.purelove.twsmanagement.util.LogUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class JoinCommand implements CommandExecutor {
    private final TWSManagement plugin;

    public JoinCommand(TWSManagement plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            LogUtils.warn("You must be a player to use this command.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(
                    Component.text("Usage: /tws joindebug")
                            .color(NamedTextColor.YELLOW)
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("joindebug")) {
            return handleDebug(player);
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        player.sendMessage(
                Component.text("Unknown subcommand.")
                        .color(NamedTextColor.RED)
        );
        return true;
    }
    private boolean handleReload(CommandSender sender) {

        if (!sender.hasPermission("twsmanagement.join.reload")) {
            sender.sendMessage(
                    Component.text("You do not have permission to do that.")
                            .color(NamedTextColor.RED)
            );
            return true;
        }

        // Reload main config.json
        plugin.getConfigurationManager().reload();

        // Reload join.json
        plugin.getJoinConfigManager().reload();
        plugin.reloadAnnouncements();
        sender.sendMessage(
                Component.text("Configuration files reloaded.")
                        .color(NamedTextColor.GREEN)
        );

        return true;
    }


    private boolean handleDebug(Player player) {

        if (!player.hasPermission("twsmanagement.join.debug")) {
            player.sendMessage(
                    Component.text("You do not have permission to do that.")
                            .color(NamedTextColor.RED)
            );
            return true;
        }

        resetPlayer(player);

        JoinConfigModel cfg = plugin.getJoinConfigManager().getConfig();
        JoinService.runFirstJoin(plugin, player, cfg);

        player.sendMessage(
                Component.text("Join debug executed.")
                        .color(NamedTextColor.GREEN)
        );


        return true;
    }

    private void resetPlayer(Player player) {
        player.getInventory().clear();
        player.setHealth(
                Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue()
        );
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        player.setGameMode(Bukkit.getDefaultGameMode());
    }
}