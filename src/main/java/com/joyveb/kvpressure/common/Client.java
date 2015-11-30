package com.joyveb.kvpressure.common;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.joyveb.kvpressure.core.ByteIterator;

public abstract class Client {
	protected PropertiesConfiguration _config;

	public PropertiesConfiguration getConfig() {
		return _config;
	}

	public void setConfig(PropertiesConfiguration config) {
		this._config = config;
	}

	public abstract void init() throws DBException ;

	public abstract void cleanup() throws DBException ;

	public abstract int read(String table, String key, Set<String> fields,HashMap<String, ByteIterator> result);

	public abstract int insert(String table, String key, HashMap<String, ByteIterator> values);

}
