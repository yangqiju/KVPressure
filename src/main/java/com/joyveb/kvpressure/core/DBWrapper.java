package com.joyveb.kvpressure.core;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.DBException;

public class DBWrapper extends Client {
	Client _db;
	CounterStore _counterStore;
	
	public DBWrapper(Client db,CounterStore counter,PropertiesConfiguration config) {
		this._db = db;
		this._counterStore = counter;
		super._config = config;
	}

	public void init() throws DBException {
		_db.init();
	}

	public void cleanup() throws DBException {
		_db.cleanup();
	}

	public int read(String table, String key, Set<String> fields,
			HashMap<String, ByteIterator> result) {
		long st = System.nanoTime();
		int res = _db.read(table, key, fields, result);
		long en = System.nanoTime();
		if(res == Constans.RESULT_OK){
			this._counterStore.read.success.incrementAndGet();
		}else if(res== Constans.RESULT_ERROR){
			this._counterStore.read.errors.incrementAndGet();
		}
		this._counterStore.read.latency.addAndGet(en-st);
		return res;
	}

	public int insert(String table, String key,
			HashMap<String, ByteIterator> values) {
		long st = System.nanoTime();
		int res = _db.insert(table, key, values);
		long en = System.nanoTime();
		if(res == Constans.RESULT_OK){
			this._counterStore.write.success.incrementAndGet();
		}else if(res== Constans.RESULT_ERROR){
			this._counterStore.write.errors.incrementAndGet();
		}
		this._counterStore.write.latency.addAndGet(en-st);
		return res;
	}
}
