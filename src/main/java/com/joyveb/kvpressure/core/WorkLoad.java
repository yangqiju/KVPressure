package com.joyveb.kvpressure.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.ConfigKey;
import com.joyveb.kvpressure.common.Constans;

@Slf4j
public class WorkLoad {

	volatile boolean stop = false;
	private List<String> fieldnames = null;
	private String table;
	
	private @Getter  long writeStartTime = 0;
	private @Getter  long readStartTime = 0;
	private int dataLength = 25;
	private  CyclicBarrier barrier ;
	private  @Getter PropertiesConfiguration config;
	
	public void setWriteStartTime(long writeStartTime) {
		if(this.writeStartTime==0){
			this.writeStartTime = writeStartTime;
		}
	}
	
	public void setReadStartTime(long readStartTime) {
		if(this.readStartTime ==0){
			this.readStartTime = readStartTime;
		}
	}
	

	
	public void init(PropertiesConfiguration config) {
		this.config = config;
		fieldnames = new ArrayList<>();
		int fieldNumber = config.getInt(ConfigKey.FIELD_NUMBER, Constans.FIELD_NUMBER_DEFAULT);
		for (int i = 0; i < fieldNumber; i++) {
			fieldnames.add(Constans.FIELD_PERFIX + i);
		}
		table = config.getString(ConfigKey.TABLE_NAME, "pressure");
		barrier = new CyclicBarrier(config.getInt(ConfigKey.THREAD_NUMBER));  
		dataLength = config.getInt(ConfigKey.FIELD_LENGTH, 25);
		
		String[] ips = config.getStringArray(ConfigKey.SERVICE_IPS);
		boolean opennmon = config.getBoolean(ConfigKey.SERVICE_NMON,false);
		if(opennmon){
			log.info("start nmon :"+Arrays.toString(ips));
			NmonManager.startNmon(ips);
		}
	}
	
	public void clearup(){
		boolean opennmon = config.getBoolean(ConfigKey.SERVICE_NMON,false);
		if(opennmon){
			String[] ips = config.getStringArray(ConfigKey.SERVICE_IPS);
			log.info("stop nmon :"+Arrays.toString(ips));
			NmonManager.stopNmon(ips);
		}
	}
	
	public void barrierAwait() throws InterruptedException, BrokenBarrierException{
		try {
			barrier.await(1, TimeUnit.MINUTES);
		} catch (TimeoutException e) {
			log.error("barrier await error.",e);
			e.printStackTrace();
		}
	}
	
	public int read(Client db,String key) {
		HashSet<String> fields=new HashSet<String>();
		for(String s : fieldnames){
			fields.add(s);
		}
		HashMap<String,ByteIterator> cells =
		        new HashMap<String,ByteIterator>();
		 db.read(table, key, fields, cells);
		return 0;
	}

	public int write(Client db, String dbkey) {
		HashMap<String, ByteIterator> values = buildValues(dbkey);
		db.insert(table,dbkey,values);
		return 0;
	}
	

	private HashMap<String, ByteIterator> buildValues(String key) {
		HashMap<String, ByteIterator> values = new HashMap<String, ByteIterator>();
		for (String fieldkey : fieldnames) {
			ByteIterator data = new RandomByteIterator(dataLength);//TODO new StringByteIterator(key);
			values.put(fieldkey, data);
		}
		return values;
	}


}
