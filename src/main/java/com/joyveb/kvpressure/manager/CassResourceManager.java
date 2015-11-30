package com.joyveb.kvpressure.manager;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.joyveb.kvpressure.common.ConfigKey;
import com.joyveb.kvpressure.common.Constans;

public class CassResourceManager {
	
	private CassResourceManager() {
	}
	
	private static class Instance {
		private static final CassResourceManager instance = new CassResourceManager();
	}
	
	public static CassResourceManager getInstance() {
		return Instance.instance;
	}

	public static final String KEYSPACE_PROPERTY = "cassandra.keyspace";
	public static final String KEYSPACE_REPLICA_PROPERTY = "cassandra.replica";
	public static final String KEYSPACE_PROPERTY_DEFAULT = "joyveb";
	public static final int KEYSPACE_REPLICA_DEFAULT = 2;
	private volatile boolean inited = false;
	
	public synchronized void init() {
		if(inited){
			return;
		}
		PropertiesConfiguration config =  PropertiesManager.getInstance().getConfig();
		String hosts[] = config.getStringArray(ConfigKey.SERVICE_IPS);
		Cluster cluster = Cluster
				.builder()
				.addContactPoints(hosts)
				.build();
		cluster.getConfiguration().getPoolingOptions()
				.setCoreConnectionsPerHost(HostDistance.LOCAL, 2)
				.setMaxConnectionsPerHost(HostDistance.LOCAL, 2);
		Session session = cluster.connect();
		String keyspace = config.getString(KEYSPACE_PROPERTY,
				KEYSPACE_PROPERTY_DEFAULT);
		int replica = config.getInt(KEYSPACE_REPLICA_PROPERTY,
				KEYSPACE_REPLICA_DEFAULT);
		Create create = SchemaBuilder.createTable(keyspace, "pressure")
				.ifNotExists().addPartitionKey(Constans.KEY_NAME, DataType.text());
		int fieldNumber = config.getInt(ConfigKey.FIELD_NUMBER, Constans.FIELD_NUMBER_DEFAULT);
		for(int i=0 ;i<fieldNumber;i++){
			create.addColumn(Constans.FIELD_PERFIX + i, DataType.text());
		}
		String createKeyspace = String
				.format("CREATE KEYSPACE IF NOT EXISTS %s  WITH REPLICATION ={ 'class' : 'SimpleStrategy', 'replication_factor' : %d };",
						keyspace, replica);
		try {
			session.execute(createKeyspace);
			session.execute(create);
		} finally {
			session.close();
			cluster.close();
		}
		System.out.println("end..");
		inited = true;
	}
}
