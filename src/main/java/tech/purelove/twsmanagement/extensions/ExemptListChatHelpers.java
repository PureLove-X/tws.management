package tech.purelove.twsmanagement.extensions;

import tech.purelove.twsmanagement.data.MojangApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import tech.purelove.twsmanagement.util.LogUtils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ExemptListChatHelpers {

    private final TextColor red = NamedTextColor.RED;
    private final TextColor yellow = NamedTextColor.YELLOW;
    private final TextColor green = NamedTextColor.GREEN;

    private final JavaPlugin plugin;
    private final MojangApi mojangApi;

    public ExemptListChatHelpers(final JavaPlugin plugin, final MojangApi mojangApi) {
        this.plugin = plugin;
        this.mojangApi = mojangApi;
    }

    public @NotNull Component buildPaginationMessage(int pageIndex,
                                                     int maxPages,
                                                     @NotNull String commandToNextPage,
                                                     @NotNull ArrayList<UUID> playerIds) {
        int nextPage = pageIndex + 1;
        int prevPage = pageIndex - 1;
        int playerIndex = pageIndex;

        Component header = Component.newline();

        for (UUID id : playerIds) {
            CompletableFuture<Player> playerIsOnServerFuture = getPlayerIsOnServerFuture(id);
            CompletableFuture<Component> playerNameFuture = getNameFuture(id, playerIsOnServerFuture);
            CompletableFuture<Component> validPlayerNameFuture = isNameValidFuture(id, playerNameFuture);

            Component playerName = null;
            try {
                playerName = validPlayerNameFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                LogUtils.error(e.getMessage());
            }

            if (playerName != null) {
                header = header.append(
                        Component.text()
                                .append(Component.text(playerIndex + " - ", yellow))
                                .append(playerName.color(green))
                                .append(Component.newline())
                );
            }

            playerIndex++;
        }

        Component prevPageComponent = Component.text("<--", red);
        Component nextPageComponent = Component.text("-->", red);

        if (prevPage > 0) { // Only allow user to go back if they can
            prevPageComponent = prevPageComponent
                .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, commandToNextPage + " " + prevPage))
                .hoverEvent(Component.text("Click to go to the previous page", yellow).asHoverEvent());
        }

        if (nextPage <= maxPages) {// Only allow user to go forward if they can
            nextPageComponent = nextPageComponent
                .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, commandToNextPage + " " + nextPage))
                .hoverEvent(Component.text("Click to go to the next page", yellow).asHoverEvent());
        }

        header = header
                .append(Component.newline())
                .append(prevPageComponent)
                .append(Component.text(" Page (", yellow))
                .append(Component.text(pageIndex + "/" + maxPages, green))
                .append(Component.text(") ", yellow))
                .append(nextPageComponent);
        
        return header;
    }

    @NotNull
    private CompletableFuture<Player> getPlayerIsOnServerFuture(@NotNull UUID id) {
        return CompletableFuture.supplyAsync(() -> this.plugin.getServer().getPlayer(id));
    }

    @NotNull
    private CompletableFuture<Component> isNameValidFuture(@NotNull UUID id,
                                                           @NotNull CompletableFuture<Component> playerNameFuture) {
        return playerNameFuture.thenApply(s -> {
            if (s == null) {
                return Component.text(id.toString());
            }
            return s;
        });
    }

    @NotNull
    private CompletableFuture<Component> getNameFuture(@NotNull UUID id,
                                                       @NotNull CompletableFuture<Player> playerIsOnServerFuture) {
        return playerIsOnServerFuture.thenCompose(player -> {
            if (player == null) {
                return this.mojangApi.getPlayerName(id);
            } else {
                return CompletableFuture.supplyAsync(player::displayName);
            }
        });
    }
}
