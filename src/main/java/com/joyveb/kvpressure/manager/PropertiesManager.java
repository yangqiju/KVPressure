package com.joyveb.kvpressure.manager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


public class PropertiesManager {

	static PropertiesConfiguration config;
	static {
		try {
			config = new PropertiesConfiguration("config.properties");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	private PropertiesManager() {
	}

	private static class Instance {
		private static final PropertiesManager instance = new PropertiesManager();
	}

	public static PropertiesManager getInstance() {
		return Instance.instance;
	}
	public PropertiesConfiguration getConfig(){
		return config;
	}
}
