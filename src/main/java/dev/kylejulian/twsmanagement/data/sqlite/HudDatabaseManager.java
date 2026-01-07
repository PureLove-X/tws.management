package dev.kylejulian.twsmanagement.data.sqlite;

import dev.kylejulian.twsmanagement.data.DatabaseConnectionManager;
import dev.kylejulian.twsmanagement.data.DatabaseManager;
import dev.kylejulian.twsmanagement.data.interfaces.IHudDatabaseManager;
import dev.kylejulian.twsmanagement.util.LogUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HudDatabaseManager extends DatabaseManager implements IHudDatabaseManager {

    public HudDatabaseManager(JavaPlugin plugin, DatabaseConnectionManager databaseConnectionManager) {
        super(plugin, databaseConnectionManager);
    }

    @Override
    public @NotNull CompletableFuture<Void> setupDefaultSchema() {
        final String sqlCommand =
                "CREATE TABLE IF NOT EXISTS hud (id INTEGER PRIMARY KEY NOT NULL, player_uuid UUID NOT NULL)";
        final String sqlIndexCommand =
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_hud_id ON hud (id)";
        final String sqlPlayerIdIndexCommand =
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_hud_player_uuid ON hud (player_uuid)";

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = this.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
                statement.execute();
            } catch (SQLException e) {
                LogUtils.warn("Unable to setup Default Schema for Hud Database table.");
                LogUtils.warn(e.getMessage());
            }

            try (Connection connection = this.getConnection();
                 PreparedStatement indexStatement = connection.prepareStatement(sqlIndexCommand);
                 PreparedStatement indexPlayerIdStatement = connection.prepareStatement(sqlPlayerIdIndexCommand)) {
                indexStatement.execute();
                indexPlayerIdStatement.execute();
            } catch (SQLException e) {
                LogUtils.warn("Unable to setup Default Indexes for Hud Database table.");
                LogUtils.warn(e.getMessage());
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> isEnabled(@NotNull UUID playerId) {
        final String sqlCommand = "SELECT id FROM hud WHERE player_uuid = ?";

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
                LogUtils.warn("Unable to execute query for Hud.");
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

    @Override
    public @NotNull CompletableFuture<Void> removePlayer(@NotNull UUID playerId) {
        final String sqlCommand = "DELETE FROM hud WHERE player_uuid = ?";

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = this.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
                statement.setObject(1, playerId);

                statement.execute();
            } catch (SQLException e) {
                LogUtils.warn("Unable to execute query for Hud.");
                LogUtils.warn(e.getMessage());
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> addPlayer(@NotNull UUID playerId) {
        final String sqlCommand = "INSERT INTO hud (player_uuid) VALUES (?)";

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = this.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sqlCommand)) {
                statement.setObject(1, playerId);

                statement.execute();
            } catch (SQLException e) {
                LogUtils.warn("Unable to execute query for Hud.");
                LogUtils.warn(e.getMessage());
            }
        });
    }
}
