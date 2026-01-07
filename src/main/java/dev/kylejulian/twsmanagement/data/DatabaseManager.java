package dev.kylejulian.twsmanagement.data;

import dev.kylejulian.twsmanagement.data.interfaces.IDatabaseManager;
import dev.kylejulian.twsmanagement.util.LogUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public abstract class DatabaseManager implements IDatabaseManager {

	private final DatabaseConnectionManager databaseConnectionManager;
	private final JavaPlugin plugin;

	public DatabaseManager(JavaPlugin plugin, DatabaseConnectionManager databaseConnectionManager) {
		this.plugin = plugin;
		this.databaseConnectionManager = databaseConnectionManager;
	}

	/**
	 * Setups the Default Database schema for the Database Manager
	 *
     * @return CompletableFuture<Void> which will set up the Database schema
     */
	public abstract @NotNull CompletableFuture<Void> setupDefaultSchema();

	protected JavaPlugin getPlugin() {
		return this.plugin;
	}

	/**
	 * Gets a Connection from the Connection pool
	 * 
	 * @return Returns the created or existing Connection
	 * @throws SQLException If a database access issue occurs
	 */
	protected Connection getConnection() throws SQLException {
		return this.databaseConnectionManager.getConnectionPool().getConnection();
	}

	/**
	 * Executes a SQL command with specified params. The params must be in the order they appear in the SQL string.
	 * @param sql SQL command to execute
	 * @param params Array of collections intended to be parameterized in the SQL query
	 * @return A CompletableFuture<Void> with the SQL command executed
	 */
	protected CompletableFuture<Void> execute(final String sql, final Object[] params) {
		return CompletableFuture.runAsync(() -> {
			try (Connection connection = this.getConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {

				if (params != null) {
					buildQueryParameters(statement, params);
				}

				statement.execute();
			} catch (SQLException e) {
				LogUtils.warn("Unable to execute query.");
				LogUtils.warn(e.getMessage());
			} catch (Exception e) {
				LogUtils.error("Unable to execute query for execute");
				LogUtils.error(e.getMessage());
			}
		});
	}

	/**
	 * Executes a SQL command with specified params. The params must be in the order they appear in the SQL string.
	 * Returns whether or not at least one row existed with for the given query.
	 * @param sql SQL command to execute
	 * @param params Array of collections intended to be parameterized in the SQL query
	 * @return A CompletableFuture<Boolean> with the SQL command executed
	 */
	protected CompletableFuture<Boolean> exists(final String sql, final Object[] params) {
		return CompletableFuture.supplyAsync(() -> {
			boolean result = false;
			ResultSet set = null;

			try (Connection connection = this.getConnection();
				 PreparedStatement statement = connection.prepareStatement(sql)) {

				if (params != null) {
					buildQueryParameters(statement, params);
				}

				set = statement.executeQuery();
				int count = 0; // Should only ever be 1

				while (set.next()) {
					count++;
				}

				result = count >= 1;
			} catch (SQLException e) {
				LogUtils.warn("Unable to execute query for exists check.");
				LogUtils.warn(e.getMessage());
			} catch (Exception e) {
				LogUtils.error("Unable to execute query for exists check.");
				LogUtils.error(e.getMessage());
			} finally {
				if (set != null) {
					try {
						set.close();
					} catch (SQLException e) {
						LogUtils.warn("Unable to execute query for exists check.");
						LogUtils.warn(e.getMessage());
					}
				}
			}

			return result;
		});
	}

	/**
	 * Adds the provided Object params to the SQL statement
	 * @param statement SQL command to add the parameters
	 * @param params The parameters that are used in the query
	 * @throws SQLException If the number of query parameters in the SQL statement do not match the number of params
	 * provided in the specified params
	 */
	private void buildQueryParameters(final PreparedStatement statement, final Object[] params) throws SQLException {
		for (int i = 0; i < params.length; i++) {
			int parameterIndex = i + 1; // First query param starts at 1
			statement.setObject(parameterIndex, params[i]);
		}
	}
}
