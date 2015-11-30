package com.joyveb.kvpressure.client;

import java.util.HashMap;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.configuration.PropertiesConfiguration;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.ConfigKey;
import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.common.DBException;
import com.joyveb.kvpressure.core.ByteIterator;
import com.joyveb.kvpressure.core.StringByteIterator;

@Slf4j
public class RedisPressureClient extends Client {

	private Jedis jedis;
	private final String table = "pressure";

	public void init() throws DBException {
		PropertiesConfiguration props = super.getConfig();
		int port = props.getInt(ConfigKey.REDIS_PORT, Protocol.DEFAULT_PORT);
		String host = props.getString(ConfigKey.SERVICE_IPS);
		jedis = new Jedis(host, port);
		jedis.connect();
		String password = props.getString(ConfigKey.REDIS_PW);
		if (password != null) {
			jedis.auth(password);
		}
	}

	public void cleanup() throws DBException {
		jedis.disconnect();
	}

	@Override
	public int read(String table, String key, Set<String> fields,
			HashMap<String, ByteIterator> result) {
		try {
			String value = jedis.hget(table, key);
			return value==null ? Constans.RESULT_ERROR: Constans.RESULT_OK;
		} catch (Exception e) {
			log.debug("redis client read error.", e);
			return Constans.RESULT_ERROR;
		}
	}

	@Override
	public int insert(String table, String key,
			HashMap<String, ByteIterator> values) {
		try {
			//TODO 获得file0 字段
			String value = StringByteIterator.getStringMap(values).get(Constans.FIELD_PERFIX + 0);
			jedis.hset(table, key,value );
			return Constans.RESULT_OK;
		} catch (Exception e) {
			log.debug("redis client write error.", e);
			return Constans.RESULT_ERROR;
		}
	}

}
