package com.joyveb.kvpressure.client;

import static com.joyveb.kvpressure.common.ConfigKey.CASS_KEYSPACE;
import static com.joyveb.kvpressure.common.ConfigKey.CASS_READ_CONSISTENCY;
import static com.joyveb.kvpressure.common.ConfigKey.CASS_WRITE_CONSISTENCY;
import static com.joyveb.kvpressure.common.ConfigKey.SERVICE_IPS;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.common.DBException;
import com.joyveb.kvpressure.core.ByteArrayByteIterator;
import com.joyveb.kvpressure.core.ByteIterator;
import com.joyveb.kvpressure.manager.CassResourceManager;

@Slf4j
public class CassandraPressureClient extends Client {

	private static Cluster cluster = null;
	private static Session session = null;

	private static ConsistencyLevel readConsistencyLevel = ConsistencyLevel.ONE;
	private static ConsistencyLevel writeConsistencyLevel = ConsistencyLevel.ALL;

	public static final String KEYSPACE_PROPERTY_DEFAULT = "joyveb";
    public static final String READ_CONSISTENCY_LEVEL_DEFAULT = "ONE";//QUORUM
    public static final String WRITE_CONSISTENCY_LEVEL_DEFAULT = "ALL";
	public static final int KEYSPACE_REPLICA_DEFAULT = 2;
	public static final int PORT_DEFAULT = 9042;
	public static  String keyspace ;
	private int id;
	public CassandraPressureClient(int id) {
		this.id = id;
	}

	@Override
	public void init() throws DBException {
		try {
			CassResourceManager.getInstance().init();
			String hosts[] = getConfig().getStringArray(SERVICE_IPS);
			String host = hosts[id%hosts.length];
			keyspace = getConfig().getString(CASS_KEYSPACE,
					KEYSPACE_PROPERTY_DEFAULT);
			cluster = Cluster.builder().withPort(PORT_DEFAULT)
					.addContactPoints(host).build();
			int threadcount = 1;
			cluster.getConfiguration().getPoolingOptions()
					.setMaxConnectionsPerHost(HostDistance.LOCAL, threadcount);
			cluster.getConfiguration().getSocketOptions()
					.setConnectTimeoutMillis(3 * 60 * 1000);
			cluster.getConfiguration().getSocketOptions()
					.setReadTimeoutMillis(3 * 60 * 1000);
			session = cluster.connect(keyspace);
			readConsistencyLevel = ConsistencyLevel.valueOf(getConfig().getString(CASS_READ_CONSISTENCY, READ_CONSISTENCY_LEVEL_DEFAULT));
            writeConsistencyLevel = ConsistencyLevel.valueOf(getConfig().getString(CASS_WRITE_CONSISTENCY, WRITE_CONSISTENCY_LEVEL_DEFAULT));
		} catch (Exception e) {
			log.info("init cassandra client is error..",e);
			throw new DBException(e);
		}
	}

	@Override
	public void cleanup() throws DBException {
		if (cluster != null) {
			cluster.close();
		}
	}

	@Override
	public int read(String table, String key, Set<String> fields,
			HashMap<String, ByteIterator> result) {
		try {
			Statement stmt;
			Select.Builder selectBuilder;
			if (fields == null) {
				selectBuilder = QueryBuilder.select().all();
			} else {
				selectBuilder = QueryBuilder.select();
				for (String col : fields) {
					((Select.Selection) selectBuilder).column(col);
				}
			}
			stmt = selectBuilder.from(keyspace, table)
					.where(QueryBuilder.eq(Constans.KEY_NAME, key)).limit(1);
			stmt.setConsistencyLevel(readConsistencyLevel);
			ResultSet rs = session.execute(stmt);
			if (!rs.isExhausted()) {
				Row row = rs.one();
				ColumnDefinitions cd = row.getColumnDefinitions();
				for (ColumnDefinitions.Definition def : cd) {
					ByteBuffer val = row.getBytesUnsafe(def.getName());
					if (val != null) {
						result.put(def.getName(),
								new ByteArrayByteIterator(val.array()));
					} else {
						result.put(def.getName(), null);
					}
				}
			}
			return Constans.RESULT_OK;
		} catch (Exception e) {
			log.debug("cassandra client read error.",e);
			return Constans.RESULT_ERROR;
		}
	}

	@Override
	public int insert(String table, String key,
			HashMap<String, ByteIterator> values) {
		try {
			Insert insertStmt = QueryBuilder.insertInto(keyspace, table);
			insertStmt.value(Constans.KEY_NAME, key);
			for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
				Object value;
				ByteIterator byteIterator = entry.getValue();
				value = byteIterator.toString();
				insertStmt.value(entry.getKey(), value);
			}
			insertStmt.setConsistencyLevel(writeConsistencyLevel);
			@SuppressWarnings("unused")
			ResultSet rs = session.execute(insertStmt);
			return Constans.RESULT_OK;
		} catch (Exception e) {
			log.debug("cassandra client write error.",e);
			return Constans.RESULT_ERROR;
		}
	}

}
