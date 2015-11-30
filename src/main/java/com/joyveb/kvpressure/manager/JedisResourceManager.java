package com.joyveb.kvpressure.manager;

import static com.joyveb.kvpressure.common.ConfigKey.CODIS_ZK_DIR;
import static com.joyveb.kvpressure.common.ConfigKey.CODIS_ZK_HOST;

import org.apache.commons.configuration.PropertiesConfiguration;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import com.joyveb.kvpressure.jodis.JedisResourcePool;
import com.joyveb.kvpressure.jodis.RoundRobinJedisPool;

public class JedisResourceManager {

	static JedisResourcePool jedisPool;

	private JedisResourceManager() {
	}

	private static class Instance {
		private static final JedisResourceManager instance = new JedisResourceManager();
	}

	public static JedisResourceManager getInstance() {
		return Instance.instance;
	}

	public synchronized Jedis getResource() {
		if (jedisPool == null) {
			PropertiesConfiguration config = PropertiesManager.getInstance().getConfig();
			JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(400);
			poolConfig.setMinIdle(1);
			String host = config.getString(CODIS_ZK_HOST, "172.16.7.82:2181");
			String zkdir = config.getString(CODIS_ZK_DIR,
					"/zk/codis/db_test/proxy");
			jedisPool = RoundRobinJedisPool.create().curatorClient(host, 30000)
					.zkProxyDir(zkdir).build();
		}
		return jedisPool.getResource();
	}

}
