package tech.purelove.twsmanagement.commands;

import tech.purelove.twsmanagement.TWSManagement;
import tech.purelove.twsmanagement.afk.events.AfkCommandEvent;
import tech.purelove.twsmanagement.commands.models.ExemptFutureModel;
import tech.purelove.twsmanagement.configuration.AfkConfigModel;
import tech.purelove.twsmanagement.configuration.MessageResolver;
import tech.purelove.twsmanagement.data.MojangApi;
import tech.purelove.twsmanagement.data.entities.EntityExemptList;
import tech.purelove.twsmanagement.data.interfaces.IExemptDatabaseManager;
import tech.purelove.twsmanagement.extensions.ExemptListChatHelpers;
import tech.purelove.twsmanagement.extensions.TextProcessor;
import tech.purelove.twsmanagement.util.LogUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record AfkCommand(JavaPlugin plugin,
                         IExemptDatabaseManager afkDatabaseManager,
                         MojangApi mojangApi) implements CommandExecutor {

    public AfkCommand(@NotNull JavaPlugin plugin, @NotNull IExemptDatabaseManager afkDatabaseManager,
                      @NotNull MojangApi mojangApi) {
        this.plugin = plugin;
        this.afkDatabaseManager = afkDatabaseManager;
        this.mojangApi = mojangApi;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player player) {
                AfkCommandEvent event = new AfkCommandEvent(player.getUniqueId());
                Runnable afkEventTask = () -> this.plugin.getServer().getPluginManager().callEvent(event);

                this.plugin.getServer().getScheduler().runTask(this.plugin, afkEventTask);
            } else {
                LogUtils.warn("You must be a player to use this command!");
            }
        } else if (args.length > 1) {
            String base = args[0];
            String command = args[1];
            if (args.length == 2 && args[0].equalsIgnoreCase("kick")) {
                return executeKickSubcommand(sender, args[1]);
            }

            if (!base.equalsIgnoreCase("exempt")) {
                TextComponent commandIsNotRecognised = Component.text()
                    .color(NamedTextColor.RED)
                    .append(Component.text("Command is not recognised"))
                    .build();

                sender.sendMessage(commandIsNotRecognised);
                return false;
            }

            if (!sender.hasPermission("tws.afk.exempt") && !sender.isOp()) {
                TextComponent noPermissions = Component.text()
                    .color(NamedTextColor.RED)
                    .append(Component.text("You do not have permissions to use this command."))
                    .build();

                sender.sendMessage(noPermissions);
                return true;
            }

            if (command.equalsIgnoreCase("list")) {
                return executeListSubcommand(sender, args);
            }

            if (command.equalsIgnoreCase("clear")) {
                return executeClearSubcommand(sender);
            }

            if (args.length < 3) {
                sender.sendMessage(NamedTextColor.RED + "You need to specify the correct number of arguments.");
                return false;
            }

            executeAddOrRemoveSubcommand(sender, args[2], command);
        }

        return true;
    }
    private boolean executeKickSubcommand(CommandSender sender, String targetName) {

        if (!sender.hasPermission("tws.afk.kick") && !sender.isOp()) {
            sender.sendMessage(
                    Component.text("You do not have permission to do that.")
                            .color(NamedTextColor.RED)
            );
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(targetName);

        if (target == null) {
            sender.sendMessage(
                    Component.text("That player is not online.")
                            .color(NamedTextColor.RED)
            );
            return true;
        }

        AfkConfigModel afkConfig =
                ((TWSManagement) plugin).getConfigurationManager()
                        .getConfig()
                        .getAfkConfig();

        MessageResolver.ResolvedMessage resolved =
                MessageResolver.resolve(plugin, Objects.requireNonNull(afkConfig).getKickMessage());

        String message =
                resolved.viewer() != null
                        ? resolved.viewer()
                        : resolved.fallback();

        Component kickMessage = TextProcessor.parse(
                message,
                target,
                target
        );

        plugin.getServer().getScheduler()
                .runTask(plugin, () -> target.kick(kickMessage));

        sender.sendMessage(
                Component.text("Kicked ")
                        .append(Component.text(target.getName()))
                        .color(NamedTextColor.GREEN)
        );

        return true;
    }

    private void executeAddOrRemoveSubcommand(@NotNull CommandSender sender, String target, String command) {
        CompletableFuture<UUID> playerIdFuture = this.mojangApi.getPlayerId(target);

        playerIdFuture
                .thenComposeAsync(afkDatabaseManager::isExempt)
                .thenCombineAsync(playerIdFuture, (isExempt, uuid) -> new ExemptFutureModel(uuid, isExempt))
                .thenComposeAsync(afkExemptFutureModel -> {
                    if (afkExemptFutureModel.getIsExempt()) {
                        // Exempt
                        if (command.equalsIgnoreCase("add")) {
                            TextComponent targetAlreadyExempt = Component.text()
                                .color(NamedTextColor.RED)
                                .append(Component.text(target))
                                .appendSpace()
                                .append(Component.text("is already AFK Kick exempt"))
                                .build();

                            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> sender.sendMessage(targetAlreadyExempt));
                            return new CompletableFuture<>();
                        }

                        TextComponent targetRemoved = Component.text()
                            .color(NamedTextColor.GREEN)
                            .append(Component.text(target))
                            .appendSpace()
                            .append(Component.text("was removed from the AFK Kick exempt list"))
                            .build();

                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> sender.sendMessage(targetRemoved));

                        return afkDatabaseManager.remove(afkExemptFutureModel.getPlayerId());
                    } else {
                        // Not exempt
                        if (command.equalsIgnoreCase("add")) {
                            TextComponent targetAdded = Component.text()
                                .color(NamedTextColor.GREEN)
                                .append(Component.text(target))
                                .appendSpace()
                                .append(Component.text("was added to AFK Kick exempt list"))
                                .build();

                            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> sender.sendMessage(targetAdded));
                            return afkDatabaseManager.add(afkExemptFutureModel.getPlayerId());
                        }
                        
                        TextComponent targetIsNotAfkList = Component.text()
                            .color(NamedTextColor.RED)
                            .append(Component.text(target))
                            .appendSpace()
                            .append(Component.text("is not AFK Kick exempt"))
                            .build();

                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> sender.sendMessage(targetIsNotAfkList));
                        return new CompletableFuture<>();
                    }
                });
    }

    private boolean executeClearSubcommand(@NotNull CommandSender sender) {
        CompletableFuture<Void> clearFuture = this.afkDatabaseManager.clear();
        clearFuture.thenComposeAsync(i -> {
            TextComponent afkListCleared = Component.text()
                .color(NamedTextColor.YELLOW)
                .append(Component.text("AFK Kick Exempt List cleared"))
                .build();

            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> sender.sendMessage(afkListCleared));

            return new CompletableFuture<>();
        });
        return true;
    }

    private boolean executeListSubcommand(@NotNull CommandSender sender, String[] args) {
        int pageIndex;

        if (args.length == 3) {
            try {
                pageIndex = Integer.parseInt(args[2]);
                if (pageIndex < 1) {
                    pageIndex = 1;
                }
            } catch (NumberFormatException e) {
                TextComponent invalidPageNumber = Component.text()
                    .color(NamedTextColor.RED)
                    .append(Component.text("You must specify a valid page number!"))
                    .build();

                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> sender.sendMessage(invalidPageNumber));
                return false;
            }
        } else {
            pageIndex = 1;
        }

        final int finalPageIndex = pageIndex;
        int pageSize = 5;
        CompletableFuture<EntityExemptList> getAfkExemptPlayers =
                this.afkDatabaseManager.getPlayers(pageIndex, pageSize);

        getAfkExemptPlayers
                .thenAcceptAsync(result -> {
                    ArrayList<UUID> playerIds = result.getPlayerIds();
                    int maxPages = result.getMaxPageCount();

                    if (playerIds.isEmpty()) {
                        TextComponent afkExemptListEmpty = Component.text()
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text("There are no results to be shown."))
                            .build();

                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> sender.sendMessage(afkExemptListEmpty));
                        return;
                    }

                    TextComponent afkListPrompt = Component.text()
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text("AFK Kick Exempt List"))
                        .build();

                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> sender.sendMessage(afkListPrompt));

                    ExemptListChatHelpers exemptListChatHelpers = new ExemptListChatHelpers(this.plugin, this.mojangApi);
                    Component baseMessage = exemptListChatHelpers.buildPaginationMessage(finalPageIndex,
                            maxPages, "/afk exempt list", playerIds);

                    this.plugin.getServer().getScheduler().runTask(this.plugin,
                            () -> sender.sendMessage(baseMessage));

                    if (!(sender instanceof Player) && finalPageIndex != maxPages) {
                        TextComponent afkText = Component.text()
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text("To fetch the next page you need to use ["))
                            .color(NamedTextColor.GREEN)
                            .append(Component.text("/afk exempt list " + (finalPageIndex + 1)))
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text("]"))
                            .build();

                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> sender.sendMessage(afkText));
                    }
                });

        return true;
    }
}
