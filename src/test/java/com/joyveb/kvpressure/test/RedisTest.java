package com.joyveb.kvpressure.test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTest {

	public static void main(String[] args) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setTestOnBorrow(true);
		config.setMaxTotal(50);
		JedisPool pool = new JedisPool(config, "192.168.22.4", 6379, 2000);
		Jedis jedis = pool.getResource();
		jedis.set("foo", "bar");
		System.out.println(jedis.get("foo"));
		jedis.close();
		pool.destroy();
		System.out.println(pool.isClosed());
		pool.close();
	}
}
