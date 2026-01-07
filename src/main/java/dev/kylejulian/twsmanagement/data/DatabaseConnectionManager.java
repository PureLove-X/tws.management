package dev.kylejulian.twsmanagement.data;

import dev.kylejulian.twsmanagement.util.LogUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import dev.kylejulian.twsmanagement.configuration.DatabaseConfigModel;

import java.io.File;
import java.sql.SQLException;

public class DatabaseConnectionManager {

	private final BasicDataSource connectionPool;

	public DatabaseConnectionManager(DatabaseConfigModel databaseConfig, String fileLocation) {
		this.connectionPool = new BasicDataSource();
		this.connectionPool.setDriverClassName("org.sqlite.JDBC");
		this.connectionPool.setUrl("jdbc:sqlite:" + fileLocation + File.separator + databaseConfig.getName());
		this.connectionPool.setInitialSize(1);
		this.connectionPool.setMaxTotal(databaseConfig.getMaxConcurrentConnections());
	}
	
	public BasicDataSource getConnectionPool() {
		return this.connectionPool;
	}

	/***
	 * Closes and releases all idle database connections
	 */
	public void closeConnections() {
		try {
			this.connectionPool.close();
		} catch (SQLException exception) {
			LogUtils.error("Error closing database");
			LogUtils.error(exception.getMessage());
		}
	}
}
