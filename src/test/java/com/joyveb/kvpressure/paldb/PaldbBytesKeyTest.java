package com.joyveb.kvpressure.paldb;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.joyveb.kvpressure.common.ByteUtils;
import com.joyveb.kvpressure.core.RandomByteIterator;
import com.linkedin.paldb.api.Configuration;
import com.linkedin.paldb.api.NotFoundException;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;
import com.linkedin.paldb.api.StoreWriter;
import com.linkedin.paldb.impl.StorageSerialization;

public class PaldbBytesKeyTest {
	File file = new File("/home/yangqiju/tmp/store2.paldb");
	int dataSize = 100;
	
	@Test
	public void test() throws NotFoundException, IOException{
		StorageSerialization storageSerialization = new StorageSerialization(new Configuration());
//			writer.put(serializedKey, serializedValue);
		StoreWriter writer = PalDB.createWriter(file);
		for (int i = 0; i < dataSize ; i++) {
			byte[] serializedKey = storageSerialization.serializeKey(i);
			byte[] value = new RandomByteIterator(25).toArray();
			System.out.println(Arrays.toString(value));
			byte[] serializedValue = storageSerialization.serializeValue(value);
			writer.put(serializedKey, serializedValue);
		}
		writer.close();
		
		StoreReader reader = PalDB.createReader(file);
		for (int i = 0; i < dataSize; i++) {
			byte[] result = reader.get(i);
			System.out.println(Arrays.toString(result));
			Assert.assertEquals(result.length,25);
		}
		reader.close();
		
	}
}
