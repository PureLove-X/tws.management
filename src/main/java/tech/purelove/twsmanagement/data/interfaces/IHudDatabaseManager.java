package tech.purelove.twsmanagement.data.interfaces;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IHudDatabaseManager extends IDatabaseManager {

    @NotNull CompletableFuture<Boolean> isEnabled(@NotNull final UUID playerId);

    @NotNull CompletableFuture<Void> removePlayer(@NotNull final UUID playerId);

    @NotNull CompletableFuture<Void> addPlayer(@NotNull final UUID playerId);
}
