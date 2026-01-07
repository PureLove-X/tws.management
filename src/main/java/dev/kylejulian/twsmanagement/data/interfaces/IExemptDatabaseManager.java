package dev.kylejulian.twsmanagement.data.interfaces;

import dev.kylejulian.twsmanagement.data.entities.EntityExemptList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

    /**
     * Manages players who are exempt from AFK kick handling.
     * <p>
     * Implementations of this interface are responsible for
     * storing, querying, and modifying AFK exemption state
     * asynchronously.
     */
public interface IExemptDatabaseManager extends IDatabaseManager {

    /**
     * Checks whether a player is exempt from being kicked for AFK.
     *
     * @param playerId UUID of the player to check
     * @return A future containing true if the player is AFK-exempt, otherwise false
     */
    @NotNull CompletableFuture<Boolean> isExempt(@NotNull UUID playerId);

    /**
     * Retrieves a paginated list of players who are exempt from AFK kick handling.
     *
     * @param pageIndex Zero-based page index (number of entries to skip)
     * @param pageSize  Maximum number of entries to return
     * @return A future containing a list of AFK-exempt players and pagination metadata
     */
    @NotNull CompletableFuture<EntityExemptList> getPlayers(final int pageIndex, final int pageSize);

    /**
     * Marks a player as exempt from AFK kick handling.
     *
     * @param playerId UUID of the player to exempt
     * @return A future that completes when the player has been added to the AFK exemption list
     */
    @NotNull CompletableFuture<Void> add(@NotNull UUID playerId);

    /**
     * Removes a player from the AFK exemption list.
     *
     * @param playerId UUID of the player to remove
     * @return A future that completes when the player is no longer AFK-exempt
     */
    @NotNull CompletableFuture<Void> remove(@NotNull UUID playerId);

    /**
     * Removes all players from the AFK exemption list.
     * <p>
     * Intended for administrative or maintenance operations.
     *
     * @return A future that completes when all AFK exemptions have been cleared
     */
    @NotNull CompletableFuture<Void> clear();
}
