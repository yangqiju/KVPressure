package com.joyveb.kvpressure.common;

import com.joyveb.kvpressure.client.AerospikePressureClient;
import com.joyveb.kvpressure.client.CassandraPressureClient;
import com.joyveb.kvpressure.client.CodisPressureClient;
import com.joyveb.kvpressure.client.JdbcDBClient;
import com.joyveb.kvpressure.client.PalDBPressureClient;
import com.joyveb.kvpressure.client.RedisPressureClient;
import com.joyveb.kvpressure.client.RiakPressureClient;
import com.joyveb.kvpressure.manager.CassResourceManager;
import com.joyveb.kvpressure.manager.PalDBResourceManager;

public enum ClientFactory {
	REDIS, CASSANDRA, RIAK, CODIS, AEROSPIKE, JDBC,PALDB;
	public static Client getClientByName(String name, int id) {
		if (ClientFactory.REDIS.name().equalsIgnoreCase(name)) {
			return new RedisPressureClient();
		} else if (ClientFactory.CASSANDRA.name().equalsIgnoreCase(name)) {
			return new CassandraPressureClient(id);
		} else if (ClientFactory.AEROSPIKE.name().equalsIgnoreCase(name)) {
			return new AerospikePressureClient();
		} else if (ClientFactory.RIAK.name().equalsIgnoreCase(name)) {
			return new RiakPressureClient();
		} else if (ClientFactory.CODIS.name().equalsIgnoreCase(name)) {
			return new CodisPressureClient(id);
		} else if (ClientFactory.JDBC.name().equalsIgnoreCase(name)) {
			return new JdbcDBClient();
		}else if (ClientFactory.PALDB.name().equalsIgnoreCase(name)) {
			return new PalDBPressureClient();
		}
		throw new RuntimeException("db name is not exists:" + name);
	}
	
	public static void stopManager(String name){
		if (ClientFactory.REDIS.name().equalsIgnoreCase(name)) {
		} else if (ClientFactory.CASSANDRA.name().equalsIgnoreCase(name)) {
		} else if (ClientFactory.AEROSPIKE.name().equalsIgnoreCase(name)) {
		} else if (ClientFactory.RIAK.name().equalsIgnoreCase(name)) {
		} else if (ClientFactory.CODIS.name().equalsIgnoreCase(name)) {
		} else if (ClientFactory.JDBC.name().equalsIgnoreCase(name)) {
		}else if (ClientFactory.PALDB.name().equalsIgnoreCase(name)) {
			PalDBResourceManager.getInstance().close();
		}
	}
}
