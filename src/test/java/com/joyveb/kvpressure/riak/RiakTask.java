package com.joyveb.kvpressure.riak;


public class RiakTask extends Thread {

	private RiakPressureClient client ;
	public RiakTask(RiakPressureClient client) {
		this.client = client;
	}
	@Override
	public void run() {
		int key = 0;
		long start = System.currentTimeMillis();
		while (!RiakStarter.STOP) {
			try {
				start = System.currentTimeMillis();
				
				client.store(this.getName()+key++);
//				client.get(this.getName()+key++);
				
				RiakStarter.successNum.incrementAndGet();
				RiakStarter.totalTransMillis.addAndGet(System
						.currentTimeMillis() - start);
				RiakStarter.requestNum.incrementAndGet();
			} catch (Exception e) {
				e.printStackTrace();
				RiakStarter.faildNum.incrementAndGet();
			}
		}
	}
}
