package com.joyveb.kvpressure.jodis;

import java.io.Closeable;

import redis.clients.jedis.Jedis;

public interface JedisResourcePool extends Closeable {

	 Jedis getResource();
	 Jedis getResource(int id);
}
