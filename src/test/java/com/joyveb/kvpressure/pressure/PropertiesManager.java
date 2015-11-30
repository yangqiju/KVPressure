package com.joyveb.kvpressure.pressure;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class PropertiesManager {
	private static PropertiesConfiguration config;

	private PropertiesManager() {
	}

	static private class PInstace {
		private final static PropertiesManager instance = new PropertiesManager();
	}

	public static PropertiesManager getInstance() {
		return PInstace.instance;
	}

	static {
		try {
			config = new PropertiesConfiguration("config.properties");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public String[] getHosts() {
		return config.getStringArray("HOSTS");
	}
	
	public RequestType getRequestType(){
		String reqeustType = config.getString("TYPE") ;
		if(RequestType.WRITE.name().equalsIgnoreCase(reqeustType)){
			return RequestType.WRITE;
		}else if(RequestType.READ.name().equalsIgnoreCase(reqeustType)){
			return RequestType.READ;
		}else if(RequestType.ALL.name().equalsIgnoreCase(reqeustType)){
			return RequestType.ALL;
		}else{
			throw new RuntimeException("reqeust type set error:"+reqeustType);
		}
	}

	public int getRunTime() {
		return config.getInt("RUN_TIME", 3600);
	}

	public int getThinkTime() {
		return config.getInt("THINK_TIME", 0);
	}
	
	public int getThreadNums() {
		return config.getInt("THREAD_NUM", 1);
	}
	
	public String getPressureType(){
		return config.getString("PRESSURE_TYPE");
	}
}
