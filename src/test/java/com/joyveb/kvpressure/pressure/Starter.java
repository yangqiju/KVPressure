package com.joyveb.kvpressure.pressure;

public class Starter {

	public static void main(String[] args) throws Exception {
		String type = PropertiesManager.getInstance().getPressureType();
		System.out.println("start pressure type:"+type);
		if (PressureServiceType.CASSANDRA.name().equalsIgnoreCase(type)) {
			new CassandraPressure().start();
		} else if (PressureServiceType.RIAK.name().equalsIgnoreCase(type)) {
			new RiakPressure().start();
		}else if(PressureServiceType.REDIS.name().equalsIgnoreCase(type)){
			new RedisPressure().start();
		} else {
			throw new RuntimeException("start error type:"+type);
		}
		System.out.println(" pressure end.");
	}
}
