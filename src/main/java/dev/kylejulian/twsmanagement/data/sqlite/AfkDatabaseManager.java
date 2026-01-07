package dev.kylejulian.twsmanagement.data.sqlite;

import dev.kylejulian.twsmanagement.data.DatabaseConnectionManager;
import dev.kylejulian.twsmanagement.data.DatabaseManager;
import dev.kylejulian.twsmanagement.data.entities.EntityExemptList;
import dev.kylejulian.twsmanagement.data.interfaces.IExemptDatabaseManager;
import dev.kylejulian.twsmanagement.util.LogUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AfkDatabaseManager extends DatabaseManager implements IExemptDatabaseManager {

	public AfkDatabaseManager(JavaPlugin plugin, DatabaseConnectionManager databaseConnectionManager) {
		super(plugin, databaseConnectionManager);
	}

	@Override
	public @NotNull CompletableFuture<Void> setupDefaultSchema() {
		final String sqlCommand =
				"CREATE TABLE IF NOT EXISTS afk_kick_exempt " +
						"(id INTEGER PRIMARY KEY NOT NULL, player_uuid UUID NOT NULL)";
		final String sqlIndexCommand =
				"CREATE UNIQUE INDEX IF NOT EXISTS idx_afk_kick_exempt_id ON afk_kick_exempt (id)";
		final String sqlPlayerIdIndexCommand =
				"CREATE UNIQUE INDEX IF NOT EXISTS idx_afk_kick_exempt_player_uuid ON afk_kick_exempt (player_uuid)";

		return CompletableFuture.runAsync(() -> {
			try (Connection connection = this.getConnection();
					PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
				statement.execute();
			} catch (SQLException e) {
				LogUtils.warn("Unable to setup Default Schema for AFK Database table.");
				LogUtils.warn(e.getMessage());
			}

			try (Connection connection = this.getConnection();
				 PreparedStatement indexStatement = connection.prepareStatement(sqlIndexCommand);
				 PreparedStatement indexPlayerIdStatement = connection.prepareStatement(sqlPlayerIdIndexCommand)) {
				indexStatement.execute();
				indexPlayerIdStatement.execute();
			} catch (SQLException e) {
				LogUtils.warn("Unable to setup Default Indexes for AFK Database table.");
				LogUtils.warn(e.getMessage());
			}
		});
	}

	/**
	 * Adds a specified Player to the exempt list
	 *
	 * @param playerId Player to remove from the Exempt list
	 */
	@Override
	public @NotNull CompletableFuture<Void> add(final @NotNull UUID playerId) {
		final String sqlCommand = "INSERT INTO afk_kick_exempt (player_uuid) VALUES (?)";

		return CompletableFuture.runAsync(() -> {
			try (Connection connection = this.getConnection();
					PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
				statement.setObject(1, playerId);
				statement.execute();
			} catch (SQLException e) {
				LogUtils.warn("Unable to execute query for AFK kick check.");
				LogUtils.warn(e.getMessage());
			}
		});
	}

	/**
	 * Removes a specified Player to the exempt list
	 *
	 * @param playerId Player to remove from the Exempt list
	 */
	@Override
	public @NotNull CompletableFuture<Void> remove(final @NotNull UUID playerId) {
		final String sqlCommand = "DELETE FROM afk_kick_exempt WHERE player_uuid = ?";

		return CompletableFuture.runAsync(() -> {
			try (Connection connection = this.getConnection();
					PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
				statement.setObject(1, playerId);
				statement.execute();
			} catch (SQLException e) {
				LogUtils.warn("Unable to execute query for AFK kick check.");
				LogUtils.warn(e.getMessage());
			}
		});
	}

	/**
	 * Returns whether or not a Player is exempt from being kicked due to inactivity
	 * 
	 * @param playerId Player to lookup
	 */
	@Override
	public @NotNull CompletableFuture<Boolean> isExempt(final @NotNull UUID playerId) {
		final String sqlCommand = "SELECT id FROM afk_kick_exempt WHERE player_uuid=?";

		return CompletableFuture.supplyAsync(() -> {
			boolean result = false;
			ResultSet set = null;

			try (Connection connection = this.getConnection();
					PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
				statement.setObject(1, playerId);

				set = statement.executeQuery();
				int count = 0; // Should only ever be 1

				while (set.next()) {
					count++;
				}

				result = count == 1;
			} catch (SQLException e) {
				LogUtils.warn("Unable to execute query for AFK kick check.");
				LogUtils.warn(e.getMessage());
			} finally {
				if (set != null) {
					try {
						set.close();
					} catch (SQLException e) {
						LogUtils.warn(e.getMessage());
					}
				}
			}

			return result;
		});
	}
	
	/**
	 * Gets a list of all AFK Kick Exempt Players
	 * @param pageIndex What page to start at. Should always be greater than 1.
	 * @param pageSize Number of results to be returned
	 */
	@Override
	public @NotNull CompletableFuture<EntityExemptList> getPlayers(final int pageIndex, final int pageSize) {
		final String sqlCommand = "SELECT * FROM afk_kick_exempt LIMIT ? OFFSET ?";
		final String sqlCountCommand = "SELECT COUNT(*) FROM afk_kick_exempt";
		int offset = pageSize * (pageIndex - 1);
		LogUtils.debug("Offset for Pagination is [" + offset + "].");

		return CompletableFuture.supplyAsync(() -> {
			EntityExemptList result = new EntityExemptList();
			ArrayList<UUID> playerIds = new ArrayList<>();
			ResultSet set = null;
			ResultSet countSet = null;
			PreparedStatement countStatement = null;

			try (Connection connection = this.getConnection();
					PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
				statement.setInt(1, pageSize);
				statement.setInt(2, offset);

				set = statement.executeQuery();
				
				while (set.next()) {
					String rowPlayerId = set.getString("player_uuid");
					UUID playerId = UUID.fromString(rowPlayerId);
					playerIds.add(playerId);
				}
				
				countStatement = connection.prepareStatement(sqlCountCommand);
				countSet = countStatement.executeQuery();
				
				while (countSet.next()) {
					int count = countSet.getInt(1);
					int maxPages =  (int) Math.ceil(count / (double) pageSize);
					result.setMaxPageCount(maxPages);
				}
				
			} catch (SQLException e) {
				LogUtils.warn("Unable to execute query for AFK kick check.");
				LogUtils.warn(e.getMessage());
			} finally {
				if (set != null) {
					try {
						set.close();
					} catch (SQLException e) {
						LogUtils.warn(e.getMessage());
					}
				}
				if (countSet != null) {
					try {
						countSet.close();
					} catch (SQLException e) {
						LogUtils.warn(e.getMessage());
					}
				}
				if (countStatement != null) {
					try {
						countStatement.close();
					} catch (SQLException e) {
						LogUtils.warn(e.getMessage());
					}
				}
			}
			
			result.setPlayerIds(playerIds);

			return result;
		});
	}

	@Override
	public @NotNull CompletableFuture<Void> clear() {
		return this.execute("DELETE FROM afk_kick_exempt", new Object[0]);
	}
}
