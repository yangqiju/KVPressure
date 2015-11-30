package com.joyveb.kvpressure.cass;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CassStarter {

	public static AtomicLong responseNum = new AtomicLong();
	public static AtomicLong requestNum = new AtomicLong();
	public static AtomicLong successNum = new AtomicLong();
	public static AtomicLong faildNum = new AtomicLong();
	public static volatile boolean STOP = false;
	public static AtomicLong totalTransMillis = new AtomicLong();
	public static int threadnum = 400;
	public static long runTime = 600;
	public static void main(String[] args) throws InterruptedException {
		// 192.168.22.204,192.168.22.205,192.168.22.207,192.168.22.202,192.168.22.208
//		String[] hosts = { "192.168.22.204", "192.168.22.205",
//				"192.168.22.207", "192.168.22.202", "192.168.22.208" };
		String[] hosts = {"172.16.7.82","172.16.7.83","172.16.7.85"};
		List<JavaDriverClient> clients = new ArrayList<JavaDriverClient>();
		for (String host : hosts) {
			JavaDriverClient client = new JavaDriverClient(host, 9042);
			client.connect("joyveb");
			clients.add(client);
		}
		ExecutorService es = Executors.newFixedThreadPool(threadnum);
		for (int i = 0; i < threadnum; i++) {
			es.execute(new CassTask(clients.get(i%hosts.length)));
		}
		long startTime = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() > (startTime + runTime * 1000)) {
				break;
			}
			TimeUnit.SECONDS.sleep(20);
			printInfo(startTime);
		}
		STOP = true;
		es.shutdownNow();
		printInfo(startTime);
	}

	protected static void printInfo(long startTime) {
		long tps = getTPS(threadnum, successNum.get(), totalTransMillis.get(),
				0);
		long time = (System.currentTimeMillis() - startTime) / 1000;
		String tpsInfo = "线程数[" + threadnum + "] 时间[" + time + "] TPS[" + tps
				+ "] success[" + successNum.get() + "] faild[" + faildNum.get()
				+ "]cost time["
				+ (double) (totalTransMillis.get() / successNum.get()) + "]";
		System.out.println(tpsInfo);
	}

	protected static long getTPS(long threadNum, long successNum,
			long totalTransMillis, long thinkTime) {
		if (successNum <= 0) {
			return 0;
		}
		return (long) (threadNum * 1000 / ((double) totalTransMillis
				/ successNum + thinkTime));
	}
}
