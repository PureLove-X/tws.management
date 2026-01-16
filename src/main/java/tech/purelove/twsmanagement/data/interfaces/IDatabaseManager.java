package tech.purelove.twsmanagement.data.interfaces;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface IDatabaseManager {

    @NotNull CompletableFuture<Void> setupDefaultSchema();
}
