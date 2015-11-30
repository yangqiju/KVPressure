package com.joyveb.kvpressure.core;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.math.RandomUtils;

import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.ConfigKey;
import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.common.DBException;
import com.joyveb.kvpressure.common.PressureType;

@Slf4j
public class ClientThread extends Thread {

	Client _db;
	WorkLoad _workLoad;

	public ClientThread(Client db, WorkLoad workLoad) {
		this._workLoad = workLoad;
		this._db = db;
	}

	@Override
	public void run() {
		try {
			_db.init();
		} catch (DBException e) {
			log.error("db init is error.",e);
			e.printStackTrace(System.out);
			return;
		}
		int totalNumber = _db.getConfig().getInt(ConfigKey.TEST_NUMBER,Constans.TEST_NUMBER_DEFAULT);
		int threads = _db.getConfig().getInt(ConfigKey.THREAD_NUMBER);
		String type = _db.getConfig().getString(ConfigKey.SERVICE_TYPE, "ALL");
		String prefix = Thread.currentThread().getName()+"_";
		int reqeustNumber = totalNumber/threads;
		if(PressureType.ALL.name().equalsIgnoreCase(type)){
			this.writeAndRead(reqeustNumber, prefix);
		}else if(PressureType.WRITE.name().equalsIgnoreCase(type)){
			this.write(reqeustNumber, prefix);
		}else if(PressureType.READ.name().equalsIgnoreCase(type)){
			this.read(reqeustNumber, prefix);
		}else if(PressureType.RREAD.name().equalsIgnoreCase(type)){
			this.randomRead(reqeustNumber, prefix);
		}else{
			throw new RuntimeException("service type config is error.");
		}
		try {
			_db.cleanup();
		} catch (DBException e) {
			log.error("db cleanup is error..",e);
			e.printStackTrace(System.out);
			return;
		}
	}
	
	private void write(int reqeustNumber,String perfix){
		int times = 0;
		String prefix = Thread.currentThread().getName()+"_";
		_workLoad.setWriteStartTime(System.currentTimeMillis());
		while (!_workLoad.stop && times < reqeustNumber) {
			_workLoad.write(_db, prefix + times);
			times++;
		}
	}
	
	private void randomRead(int reqeustNumber,String prefix){
		int times = 0;
		_workLoad.setReadStartTime(System.currentTimeMillis());
		times = 0;
		while (!_workLoad.stop && times < reqeustNumber) {
			_workLoad.read(_db, prefix + RandomUtils.nextInt(reqeustNumber));
			times++;
		}
	}
	
	private void read(int reqeustNumber,String prefix){
		int times = 0;
		_workLoad.setReadStartTime(System.currentTimeMillis());
		times = 0;
		while (!_workLoad.stop && times < reqeustNumber) {
			_workLoad.read(_db, prefix + times);
			times++;
		}
	}
	
	private void writeAndRead(int reqeustNumber,String prefix){
		int times = 0;
		_workLoad.setWriteStartTime(System.currentTimeMillis());
		while (!_workLoad.stop && times < reqeustNumber) {
			_workLoad.write(_db, prefix + times);
			times++;
		}
		try {
			_workLoad.barrierAwait();
		} catch (Exception e) {
			log.error("wait thread is error.",e);
			e.printStackTrace(System.out);
			return ;
		}
		_workLoad.setReadStartTime(System.currentTimeMillis());
		times = 0;
		while (!_workLoad.stop && times < reqeustNumber) {
			_workLoad.read(_db, prefix + times);
			times++;
		}
	}
}
