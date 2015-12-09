package com.joyveb.kvpressure.disruptor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import com.joyveb.kvpressure.common.ByteUtils;
import com.joyveb.kvpressure.common.Constans;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

public class DisruptorPaldbTest {
	final static int dataSize = 10000000;
	private final ExecutorService executor = Executors.newCachedThreadPool();

	@Test
	// @Ignore
	public void test() throws InterruptedException {
//		thread : 4
//		paldb pressure: TPS:1701782.0180152473
//		WaitStrategy strategy = new BlockingWaitStrategy();//TPS:1568515.5317382445
//		WaitStrategy strategy = new SleepingWaitStrategy();//TPS:1555607.208127424
//		WaitStrategy strategy = new YieldingWaitStrategy();//TPS:1528672.45316658
		WaitStrategy strategy = new BusySpinWaitStrategy();//TPS:1561718.9975063512
		Disruptor<KVEvent> disruptor = new Disruptor<DisruptorPaldbTest.KVEvent>(
				KVEvent.FACTORY, 1<<16, executor, ProducerType.MULTI,
				strategy);
		RingBuffer<KVEvent> ringBuffer = disruptor.getRingBuffer();
		disruptor.handleExceptionsWith(new FatalExceptionHandler());

		int threads = 2;
		int iterations = 10000000;
		int publisherCount = threads;

		CyclicBarrier barrier = new CyclicBarrier(publisherCount);
		CountDownLatch latch = new CountDownLatch(publisherCount);

		KVEventHander handler1 = new KVEventHander();
		disruptor.handleEventsWith(handler1);

		Publisher[] publishers = new Publisher[publisherCount];
		for (int i = 0; i < publisherCount; i++) {
			publishers[i] = new Publisher(ringBuffer, iterations, barrier,
					latch);
		}
		disruptor.start();
		long readStartTime = System.nanoTime();
		for (Publisher publisher : publishers) {
			executor.execute(publisher);
		}
		latch.await();
		while (ringBuffer.getCursor() < (iterations - 1)) {
			LockSupport.parkNanos(1);
		}
		disruptor.shutdown();

		for (Publisher publisher : publishers) {
			assertThat(publisher.failed, is(false));
		}
		int number = 0;
		number += handler1.successNumber;
		System.out.println("pressure end . success number:" + number);
		long costTime = System.nanoTime() - readStartTime;
		System.out.println("read end.. cost time:" + costTime
				/ Constans.NANO_MILLIS);
		System.out.println("TPS:"
				+ (number / ((costTime / Constans.NANO_MILLIS) / 1000)));
	}

	private static class KVEvent {
		public byte[] key;
		public byte[] value;
		public long sequence;
		public CountDownLatch latch = new CountDownLatch(1);

		public static final EventFactory<KVEvent> FACTORY = new EventFactory<DisruptorPaldbTest.KVEvent>() {
			@Override
			public KVEvent newInstance() {
				return new KVEvent();
			}
		};
	}

	private static class KVEventHander implements EventHandler<KVEvent> {
		File file = new File("/home/yangqiju/tmp/store.paldb");
		StoreReader reader = PalDB.createReader(file);
		int successNumber = 0;

		public KVEventHander() {
		}

		@Override
		public void onEvent(KVEvent event, long sequence, boolean endOfBatch)
				throws Exception {
			if (sequence != event.sequence) {
				throw new RuntimeException("sequence is not equal..");
			}
			byte[] key = event.key;
			byte[] result = reader.get(key);
			event.value = result;
			// System.out.println(Arrays.toString(key)+" ::"
			// +Arrays.toString(result));
			if (!Arrays.equals(key, result)) {
				throw new RuntimeException(
						"result is null or  length is not 25..");
			}
			successNumber++;
//			event.latch.countDown();
		}
	}

	private static class Publisher implements Runnable {
		private final RingBuffer<KVEvent> ringBuffer;
		private final CyclicBarrier barrier;
		private final int iterations;
		private final CountDownLatch shutdownLatch;
		public boolean failed = false;

		public Publisher(RingBuffer<KVEvent> ringBuffer, int iterations,
				CyclicBarrier barrier, CountDownLatch shutdownLatch) {
			this.ringBuffer = ringBuffer;
			this.barrier = barrier;
			this.iterations = iterations;
			this.shutdownLatch = shutdownLatch;
		}

		@Override
		public void run() {
			try {
				barrier.await();
				int i = iterations;
				while (i-- != 0) {
					long next = this.ringBuffer.next();
					KVEvent kvEvent = this.ringBuffer.get(next);
					kvEvent.sequence = next;
					kvEvent.key = ByteUtils.intToBytes(RandomUtils
							.nextInt(dataSize));
					ringBuffer.publish(next);
//					kvEvent.latch.await();
				}
			} catch (Exception e) {
				failed = true;
				e.printStackTrace();
			} finally {
				shutdownLatch.countDown();
			}
		}
	}
}
