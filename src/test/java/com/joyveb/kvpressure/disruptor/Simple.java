package com.joyveb.kvpressure.disruptor;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.math.RandomUtils;

import com.joyveb.kvpressure.common.ByteUtils;
import com.joyveb.kvpressure.common.Constans;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class Simple {
	static int number = 0;
	static File file = new File("/home/yangqiju/tmp/store.paldb");
	static long dataSize = 10000000;
	static StoreReader reader = PalDB.createReader(file);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		ExecutorService exec = Executors.newCachedThreadPool();
		// Preallocate RingBuffer with 1024 ValueEvents
		Disruptor<ValueEvent> disruptor = new Disruptor<ValueEvent>(
				ValueEvent.EVENT_FACTORY, 1024, exec);
		final EventHandler<ValueEvent> handler = new EventHandler<ValueEvent>() {
			public void onEvent(final ValueEvent event, final long sequence,
					final boolean endOfBatch) throws Exception {
				byte[] result = reader.get(ByteUtils.intToBytes(RandomUtils
						.nextInt((int) dataSize)));
				if (result.length != 25) {
					throw new RuntimeException("error..");
				}
			}
		};
		disruptor.handleEventsWith(handler);
		RingBuffer<ValueEvent> ringBuffer = disruptor.start();
		long readStartTime = System.nanoTime();
		for (long i = 0; i < dataSize; i++) {
			long seq = ringBuffer.next();
			ValueEvent valueEvent = ringBuffer.get(seq);
			valueEvent.setValue(i + "");
			ringBuffer.publish(seq);
		}
		disruptor.shutdown();
		exec.shutdown();
		long readEndTime = System.nanoTime() - readStartTime;
		System.out.println("read end.. cost time:" + readEndTime
				/ Constans.NANO_MILLIS);
		System.out.println("TPS:"
				+ (dataSize / ((readEndTime / Constans.NANO_MILLIS) / 1000)));
		System.out.println(number);
	}

	static class ValueEvent {
		private String value;
		private String result;

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public final static EventFactory<ValueEvent> EVENT_FACTORY = new EventFactory<ValueEvent>() {
			public ValueEvent newInstance() {
				return new ValueEvent();
			}
		};
	}
}