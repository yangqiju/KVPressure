package com.joyveb.kvpressure.paldb;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.joyveb.kvpressure.common.ByteUtils;
import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.core.RandomByteIterator;
import com.linkedin.paldb.api.Configuration;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;
import com.linkedin.paldb.api.StoreWriter;
import com.linkedin.paldb.impl.StorageSerialization;

public class PaldbExample {
	File file = new File("/home/yangqiju/tmp/store.paldb");
	int dataSize = 10000000;
	int loopSize = dataSize*4;
	StorageSerialization storageSerialization = new StorageSerialization(
			new Configuration());

	@Test
	// @Ignore
	public void test() throws IOException {
		 this.write();
//		this.read();
	}

	public void write() throws IOException {
		StoreWriter writer = PalDB.createWriter(file);
		long writeStartTime = System.nanoTime();
		for (int i = 0; i < dataSize; i++) {
			byte[] serializedKey = storageSerialization.serializeKey(ByteUtils
					.intToBytes(i));
			 byte[] value = new RandomByteIterator(25).toArray();
//			byte[] value = ByteUtils.intToBytes(i);
			byte[] serializedValue = storageSerialization.serializeValue(value);
			writer.put(serializedKey, serializedValue);
		}
		writer.close();
		long writeEndTime = System.nanoTime() - writeStartTime;
		System.out.println("write end..cost time:"
				+ (writeEndTime / Constans.NANO_MILLIS));
		System.out.println("all write is end..");
	}

	public void read() {
		Configuration config = new Configuration();
		config.set(Configuration.MMAP_DATA_ENABLED, "true");
		config.set(Configuration.CACHE_ENABLED, "false");
		StoreReader reader = PalDB.createReader(file, config);
		long readStartTime = System.nanoTime();
		long trancost = 0;
		for (int i = 0; i < loopSize; i++) {
			long costStart = System.nanoTime();
			byte[] result = reader.get(ByteUtils.intToBytes(RandomUtils
					.nextInt(dataSize)));
			// System.out.println(result);
			trancost = trancost + (System.nanoTime() - costStart);
//			Assert.assertEquals(25, result.length);
		}
		reader.close();
		long readEndTime = System.nanoTime() - readStartTime;
		System.out.println("read end.. cost time:" + readEndTime
				/ Constans.NANO_MILLIS);
		System.out.println("TPS:"
				+ (loopSize / ((readEndTime / Constans.NANO_MILLIS) / 1000)));
		System.out.println("cost time::" + (trancost / dataSize) + " nano");
		System.gc();
	}

}
