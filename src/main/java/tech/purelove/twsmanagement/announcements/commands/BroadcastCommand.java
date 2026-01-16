package tech.purelove.twsmanagement.announcements.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import tech.purelove.twsmanagement.announcements.AnnouncementRegistry;
import tech.purelove.twsmanagement.announcements.Announcement;
import tech.purelove.twsmanagement.extensions.TextProcessor;

import java.util.List;

public class BroadcastCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "tws.broadcast";

    private final AnnouncementRegistry registry;

    public BroadcastCommand(AnnouncementRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean onCommand(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            String[] args
    ) {

        // Permission check (console always allowed)
        if (sender instanceof Player player && !player.hasPermission(PERMISSION)) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        // /broadcast
        if (args.length == 0) {
            sender.sendMessage("§eAvailable broadcasts:");
            for (String name : registry.names()) {
                sender.sendMessage(" §7- §f" + name);
            }
            return true;
        }

        // /broadcast <name>
        Announcement announcement = registry.get(args[0]);
        if (announcement == null) {
            sender.sendMessage("§cUnknown broadcast: §f" + args[0]);
            sender.sendMessage("§7Use §f/broadcast §7to see available options.");
            return true;
        }

        Player senderPlayer = (sender instanceof Player p) ? p : null;

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            for (String line : announcement.lines()) {
                viewer.sendMessage(
                        TextProcessor.parse(line, viewer, senderPlayer)
                );
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String alias,
            String[] args
    ) {

        if (sender instanceof Player player && !player.hasPermission(PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return registry.names().stream()
                    .filter(name -> name.startsWith(prefix))
                    .toList();
        }

        return List.of();
    }
}

