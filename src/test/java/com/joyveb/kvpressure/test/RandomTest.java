package com.joyveb.kvpressure.test;

import java.util.Random;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;

public class RandomTest {

	@Test
	@Ignore
	public void test() {
		int reqeustNumber = 1000000000;
		Random random = new Random(reqeustNumber);
		for (int i = 0; i < 1000000; i++) {
			System.out.println(RandomUtils.nextInt(reqeustNumber));
//			System.out.println(random.nextInt(reqeustNumber));
		}
	}
}
