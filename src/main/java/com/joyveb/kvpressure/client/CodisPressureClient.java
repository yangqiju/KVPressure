package com.joyveb.kvpressure.client;

import java.util.HashMap;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.common.DBException;
import com.joyveb.kvpressure.core.ByteIterator;
import com.joyveb.kvpressure.core.StringByteIterator;
import com.joyveb.kvpressure.manager.JedisResourceManager;

@Slf4j
public class CodisPressureClient extends Client {

	private Jedis jedis;
	private int id;
	public CodisPressureClient(int id) {
		this.id = id;
	}

	public void init() throws DBException {
		jedis = JedisResourceManager.getInstance().getResource();
	}

	public void cleanup() throws DBException {
		try {
			jedis.close();
		} catch (Exception e) {
			throw new DBException(e);
		}
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
