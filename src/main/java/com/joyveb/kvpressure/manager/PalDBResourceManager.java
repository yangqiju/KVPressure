package com.joyveb.kvpressure.manager;

import java.io.File;

import com.joyveb.kvpressure.common.ConfigKey;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;

public class PalDBResourceManager {

	File file = null;
	StoreReader reader = null;

	private PalDBResourceManager() {
		String path = PropertiesManager.getInstance().getConfig().getString(ConfigKey.PALDB_FILE);
		file = new File(path);
		reader = PalDB.createReader(file);
	}

	private static class Instance {
		private static final PalDBResourceManager instance = new PalDBResourceManager();
	}

	public static PalDBResourceManager getInstance() {
		return Instance.instance;
	}

	public synchronized StoreReader getResource() {
		return reader;
	}
	
	public synchronized void close(){
		reader.close();
	}

}
