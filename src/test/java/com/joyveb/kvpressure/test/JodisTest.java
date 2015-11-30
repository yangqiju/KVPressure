package com.joyveb.kvpressure.test;

import org.junit.Test;

import redis.clients.jedis.Jedis;

import com.joyveb.kvpressure.jodis.JedisResourcePool;
import com.joyveb.kvpressure.jodis.RoundRobinJedisPool;

public class JodisTest {

	@Test
	public void test(){
		JedisResourcePool jedisPool = RoundRobinJedisPool.create()
		        .curatorClient("172.16.7.82:2181", 30000).zkProxyDir("/zk/codis/db_test/proxy").build();
		try (Jedis jedis = jedisPool.getResource()) {
		    jedis.set("foo", "bar");
		    String value = jedis.get("foo");
		    System.out.println(value);
		}
	}
}
