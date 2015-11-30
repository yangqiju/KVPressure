package com.joyveb.kvpressure.manager;

import static com.joyveb.kvpressure.common.ConfigKey.JDBC_CONNECTION_PASSWD;
import static com.joyveb.kvpressure.common.ConfigKey.JDBC_CONNECTION_URL;
import static com.joyveb.kvpressure.common.ConfigKey.JDBC_CONNECTION_USER;
import static com.joyveb.kvpressure.common.ConfigKey.JDBC_DRIVER_CLASS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.joyveb.kvpressure.common.ConfigKey;
import com.joyveb.kvpressure.common.Constans;

public class JdbcManager {

	private JdbcManager() {
	}
	
	private static class Instance {
		private static final JdbcManager instance = new JdbcManager();
	}
	
	public static JdbcManager getInstance() {
		return Instance.instance;
	}
	
	private volatile boolean inited = false;
	
	public synchronized void init() throws SQLException {
		if(inited){
			return;
		}
		PropertiesConfiguration props = PropertiesManager.getInstance().getConfig();
		int fieldNumber = props.getInt(ConfigKey.FIELD_NUMBER, Constans.FIELD_NUMBER_DEFAULT);
		String urls = props.getString(JDBC_CONNECTION_URL);
		String user = props.getString(JDBC_CONNECTION_USER);
		String passwd = props.getString(JDBC_CONNECTION_PASSWD);
		String driver = props.getString(JDBC_DRIVER_CLASS);
		String tablename = props.getString(Constans.TABLE_NAME);
		if (driver == null || user == null || urls == null) {
			throw new SQLException("Missing connection information.");
		}

		Connection conn = null;
		try {
			Class.forName(driver);

			conn = DriverManager.getConnection(urls, user, passwd);
			Statement stmt = conn.createStatement();

			StringBuilder sql = new StringBuilder("DROP TABLE IF EXISTS ");
			sql.append(tablename);
			sql.append(";");

			stmt.execute(sql.toString());

			sql = new StringBuilder("CREATE TABLE ");
			sql.append(tablename);
			sql.append("(").append(Constans.KEY_NAME);
			sql.append("  VARCHAR PRIMARY KEY");

			for (int idx = 0; idx < fieldNumber; idx++) {
				sql.append(", ").append(Constans.FIELD_PERFIX );
				sql.append(idx);
				sql.append(" VARCHAR");
			}
			sql.append(");");
			stmt.execute(sql.toString());
			System.out.println("Table " + tablename + " created..");
		} catch (ClassNotFoundException e) {
			throw new SQLException("JDBC Driver class not found.");
		} finally {
			if (conn != null) {
				System.out.println("Closing database connection.");
				conn.close();
			}
		}
		inited = true;
	}
}
