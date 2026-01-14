package dev.kylejulian.twsmanagement.commands.tabcompleters;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public record AfkTabCompleter(JavaPlugin plugin) implements TabCompleter {

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            String[] args
    ) {
        ArrayList<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("exempt");
            suggestions.add("kick");
            return suggestions;
        }

        // /afk exempt <sub>
        if (args.length == 2 && args[0].equalsIgnoreCase("exempt")) {
            suggestions.add("add");
            suggestions.add("remove");
            suggestions.add("list");
            suggestions.add("clear");
            return suggestions;
        }

        // /afk kick <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("kick")) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
            return suggestions;
        }

        // /afk exempt add/remove <player>
        if (args.length == 3 && args[0].equalsIgnoreCase("exempt")) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
            return suggestions;
        }

        return suggestions;
    }
}
