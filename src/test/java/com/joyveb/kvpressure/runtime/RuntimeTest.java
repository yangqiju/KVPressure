package com.joyveb.kvpressure.runtime;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.joyveb.kvpressure.core.NmonManager;

public class RuntimeTest {

	@Test
	public void test() {
		String[] ips = {"172.16.7.82","172.16.7.83","172.16.7.85"};
//		NmonManager.startNmon(ips);
//		try {
//			TimeUnit.SECONDS.sleep(10);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		NmonManager.stopNmon(ips);
	}
}
