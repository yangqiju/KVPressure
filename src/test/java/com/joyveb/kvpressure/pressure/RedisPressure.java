package com.joyveb.kvpressure.pressure;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPressure extends BasePressure {

	private JedisPool pool;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		JedisPoolConfig config = new JedisPoolConfig();
		config.setTestOnBorrow(true);
		config.setMaxTotal(400);
		pool = new JedisPool(config, hosts[0], 6379, 2000);
	}

	@Override
	protected void setDown() {
		super.setDown();
		if (pool != null)
			pool.destroy();
	}

	@Override
	protected Thread getPressureThread(int i) {
		return new RedisThread(pool.getResource());
	}

	public static class RedisThread extends Thread {
		private Jedis jedis;

		public RedisThread(Jedis jedis) {
			this.jedis = jedis;
		}

		@Override
		public void run() {
			long start = 0;
			while (!BasePressure.STOP) {
				try {
					start = System.nanoTime();
					store();
//					get();
					BasePressure.successNum.addAndGet(1);
					BasePressure.totalTransMillis.addAndGet(System.nanoTime() - start);
					BasePressure.requestNum.addAndGet(1);
					TimeUnit.MILLISECONDS.sleep(BasePressure.thinkTime);
				} catch (Exception e) {
					e.printStackTrace();
					BasePressure.faildNum.addAndGet(1);
				}
			}
			jedis.close();
		}

		private void store() throws Exception {
//			jedis.hset("redispressure", UUID.randomUUID().toString(), "value");
			jedis.set("redispressure"+UUID.randomUUID().toString(), "value");
		}
		
		private void get(){
			jedis.hget("redispressure", UUID.randomUUID().toString());
		}
	}
}
