package com.joyveb.kvpressure.client;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;

import com.joyveb.kvpressure.common.ByteUtils;
import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.common.DBException;
import com.joyveb.kvpressure.core.ByteIterator;
import com.joyveb.kvpressure.manager.PalDBResourceManager;
import com.linkedin.paldb.api.StoreReader;

public class PalDBPressureClient extends Client {
	int dataSize = 10000000;
	StoreReader reader = null;
	
	@Override
	public void init() throws DBException {
		this.reader = PalDBResourceManager.getInstance().getResource();
	}

	@Override
	public void cleanup() throws DBException {
	}

	@Override
	public int read(String table, String key, Set<String> fields,
			HashMap<String, ByteIterator> result) {
		byte[] bytes = reader.get(ByteUtils.intToBytes(RandomUtils
				.nextInt(dataSize)));
		if(bytes==null || bytes.length!=25){
			return Constans.RESULT_ERROR;
		}
		return Constans.RESULT_OK;
	}

	@Override
	public int insert(String table, String key,
			HashMap<String, ByteIterator> values) {
		return 0;
	}
}
