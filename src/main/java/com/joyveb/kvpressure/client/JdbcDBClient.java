package com.joyveb.kvpressure.client;

import static com.joyveb.kvpressure.common.ConfigKey.JDBC_AUTO_COMMIT;
import static com.joyveb.kvpressure.common.ConfigKey.JDBC_CONNECTION_PASSWD;
import static com.joyveb.kvpressure.common.ConfigKey.JDBC_CONNECTION_URL;
import static com.joyveb.kvpressure.common.ConfigKey.JDBC_CONNECTION_USER;
import static com.joyveb.kvpressure.common.ConfigKey.JDBC_DRIVER_CLASS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.common.DBException;
import com.joyveb.kvpressure.core.ByteIterator;
import com.joyveb.kvpressure.core.StringByteIterator;
import com.joyveb.kvpressure.manager.JdbcManager;

/**
 * <ul>
 * <li><b>db.driver</b> The JDBC driver class to use.</li>
 * <li><b>db.url</b> The Database connection URL.</li>
 * <li><b>db.user</b> User name for the connection.</li>
 * <li><b>db.passwd</b> Password for the connection.</li>
 * </ul>
 */
@Slf4j
public class JdbcDBClient extends Client  {

	private ArrayList<Connection> conns;
	private PropertiesConfiguration props;
	private static final String DEFAULT_PROP = "";
	private ConcurrentMap<StatementType, PreparedStatement> cachedStatements;

	private static class StatementType {

		enum Type {
			INSERT(1), DELETE(2), READ(3), UPDATE(4), SCAN(5), ;
			int internalType;

			private Type(int type) {
				internalType = type;
			}

			int getHashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + internalType;
				return result;
			}
		}

		Type type;
		int shardIndex;
		int numFields;
		String tableName;

		StatementType(Type type, String tableName, int numFields,
				int _shardIndex) {
			this.type = type;
			this.tableName = tableName;
			this.numFields = numFields;
			this.shardIndex = _shardIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + numFields + 100 * shardIndex;
			result = prime * result
					+ ((tableName == null) ? 0 : tableName.hashCode());
			result = prime * result + ((type == null) ? 0 : type.getHashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StatementType other = (StatementType) obj;
			if (numFields != other.numFields)
				return false;
			if (shardIndex != other.shardIndex)
				return false;
			if (tableName == null) {
				if (other.tableName != null)
					return false;
			} else if (!tableName.equals(other.tableName))
				return false;
			if (type != other.type)
				return false;
			return true;
		}
	}

	private int getShardIndexByKey(String key) {
		int ret = Math.abs(key.hashCode()) % conns.size();
		return ret;
	}

	private Connection getShardConnectionByKey(String key) {
		return conns.get(getShardIndexByKey(key));
	}

	private void cleanupAllConnections() throws SQLException {
		for (Connection conn : conns) {
			conn.close();
		}
	}

	@Override
	public void init() throws DBException {
		props = getConfig();
		String urls = props.getString(JDBC_CONNECTION_URL, DEFAULT_PROP);
		String user = props.getString(JDBC_CONNECTION_USER, DEFAULT_PROP);
		String passwd = props.getString(JDBC_CONNECTION_PASSWD, DEFAULT_PROP);
		String driver = props.getString(JDBC_DRIVER_CLASS);

		boolean autoCommit = props.getBoolean(JDBC_AUTO_COMMIT);
		try {
			JdbcManager.getInstance().init();
			if (driver != null) {
				Class.forName(driver);
			}
			int shardCount = 0;
			conns = new ArrayList<Connection>(3);
			for (String url : urls.split(",")) {
				System.out.println("Adding shard node URL: " + url);
				Connection conn = DriverManager
						.getConnection(url, user, passwd);
				// Since there is no explicit commit method in the DB interface,
				// all
				// operations should auto commit, except when explicitly told
				// not to
				// (this is necessary in cases such as for PostgreSQL when
				// running a
				// scan workload with fetchSize)
				conn.setAutoCommit(autoCommit);

				shardCount++;
				conns.add(conn);
			}

			System.out.println("Using " + shardCount + " shards");

			cachedStatements = new ConcurrentHashMap<StatementType, PreparedStatement>();
		} catch (ClassNotFoundException e) {
			System.err.println("Error in initializing the JDBS driver: " + e);
			throw new DBException(e);
		} catch (SQLException e) {
			System.err.println("Error in database operation: " + e);
			throw new DBException(e);
		} catch (NumberFormatException e) {
			System.err.println("Invalid value for fieldcount property. " + e);
			throw new DBException(e);
		}
	}

	@Override
	public void cleanup() throws DBException {
		try {
			cleanupAllConnections();
		} catch (SQLException e) {
			log.error("Error in closing the connection. " + e);
			throw new DBException(e);
		}
	}

	private PreparedStatement createAndCacheInsertStatement(
			StatementType insertType, String key) throws SQLException {
		StringBuilder insert = new StringBuilder("INSERT INTO ");
		insert.append(insertType.tableName);
		insert.append(" VALUES(?");
		for (int i = 0; i < insertType.numFields; i++) {
			insert.append(",?");
		}
		insert.append(");");
		PreparedStatement insertStatement = getShardConnectionByKey(key)
				.prepareStatement(insert.toString());
		PreparedStatement stmt = cachedStatements.putIfAbsent(insertType,
				insertStatement);
		if (stmt == null)
			return insertStatement;
		else
			return stmt;
	}

	private PreparedStatement createAndCacheReadStatement(
			StatementType readType, String key) throws SQLException {
		StringBuilder read = new StringBuilder("SELECT * FROM ");
		read.append(readType.tableName);
		read.append(" WHERE ");
		read.append(Constans.KEY_NAME);
		read.append(" = ");
		read.append("?;");
		PreparedStatement readStatement = getShardConnectionByKey(key)
				.prepareStatement(read.toString());
		PreparedStatement stmt = cachedStatements.putIfAbsent(readType,
				readStatement);
		if (stmt == null)
			return readStatement;
		else
			return stmt;
	}

	@Override
	public int read(String tableName, String key, Set<String> fields,
			HashMap<String, ByteIterator> result) {
		if (tableName == null || key == null) {
			throw new RuntimeException(" tablename or key can't be null");
		}
		try {
			StatementType type = new StatementType(StatementType.Type.READ,
					tableName, 1, getShardIndexByKey(key));
			PreparedStatement readStatement = cachedStatements.get(type);
			if (readStatement == null) {
				readStatement = createAndCacheReadStatement(type, key);
			}
			readStatement.setString(1, key);
			ResultSet resultSet = readStatement.executeQuery();
			if (!resultSet.next()) {
				resultSet.close();
				return Constans.RESULT_OK;
			}
			if (result != null && fields != null) {
				for (String field : fields) {
					String value = resultSet.getString(field);
					result.put(field, new StringByteIterator(value));
				}
			}
			resultSet.close();
			return Constans.RESULT_OK;
		} catch (SQLException e) {
			log.error("Error in processing read of table " + tableName + ": "
					+ e);
			return Constans.RESULT_ERROR;
		}
	}

	@Override
	public int insert(String tableName, String key,
			HashMap<String, ByteIterator> values) {
		if (tableName == null || key == null) {
			throw new RuntimeException(" tablename or key can't be null");
		}
		try {
			int numFields = values.size();
			StatementType type = new StatementType(StatementType.Type.INSERT,
					tableName, numFields, getShardIndexByKey(key));
			PreparedStatement insertStatement = cachedStatements.get(type);
			if (insertStatement == null) {
				insertStatement = createAndCacheInsertStatement(type, key);
			}
			insertStatement.setString(1, key);
			int index = 2;
			for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
				String field = entry.getValue().toString();
				insertStatement.setString(index++, field);
			}
			int result = insertStatement.executeUpdate();
			if (result == 1)
				return Constans.RESULT_OK;
			else
				return Constans.RESULT_ERROR;
		} catch (SQLException e) {
			log.error("Error in processing insert to table: " + tableName + e);
			return Constans.RESULT_ERROR;
		}
	}

}
