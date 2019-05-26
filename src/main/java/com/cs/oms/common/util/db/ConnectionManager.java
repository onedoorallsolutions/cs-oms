package com.cs.oms.common.util.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

public class ConnectionManager {
	private final static Logger logger = Logger.getLogger(ConnectionManager.class);
	private final BasicDataSource targetReferenceDb;

	public ConnectionManager(String connectionString, int initSize, int maxActive, int minIdle,
			Collection<String> initSqls) {
		this.targetReferenceDb = initializePool(connectionString, initSize, maxActive, minIdle, initSqls);
	}

	public Connection getConnection() throws SQLException {
		return targetReferenceDb != null ? targetReferenceDb.getConnection() : null;
	}

	private BasicDataSource initializePool(String connectionString, int initSize, int maxActive, int minIdle,
			Collection<String> initSqls) {
		BasicDataSource targetReferenceDb = null;

		if (null != connectionString) {
			targetReferenceDb = createPooledDataSourceFromConnection(connectionString);
			targetReferenceDb.setConnectionInitSqls(initSqls);
			targetReferenceDb.setInitialSize(initSize);
			targetReferenceDb.setMaxActive(maxActive);
			targetReferenceDb.setMinIdle(minIdle);
		}
		return targetReferenceDb;
	}

	private BasicDataSource createPooledDataSourceFromConnection(String connectionString) {
		String[] props = connectionString.trim().split(",");
		String driverClassName = getPropertyFromArray(props, 0);
		String url = getPropertyFromArray(props, 1);
		String userName = getPropertyFromArray(props, 2);
		String password = getPropertyFromArray(props, 3);

		BasicDataSource basicDataSource = new BasicDataSource();
		basicDataSource.setDriverClassName(driverClassName);
		basicDataSource.setUrl(url);
		basicDataSource.setUsername(userName);
		basicDataSource.setPassword(password);
		logger.info("Created Pool Connection factory");
		return basicDataSource;
	}

	private String getPropertyFromArray(String[] props, int index) {
		try {
			return props[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}

	}

}
