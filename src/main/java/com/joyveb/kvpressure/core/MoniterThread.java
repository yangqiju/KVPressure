package com.joyveb.kvpressure.core;

import org.apache.commons.lang3.time.DateFormatUtils;

import lombok.extern.slf4j.Slf4j;

import com.joyveb.kvpressure.common.ConfigKey;
import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.common.PressureType;
import com.joyveb.kvpressure.core.CounterStore.Current;

@Slf4j
public class MoniterThread extends Thread {

	CounterStore _counterStore;
	WorkLoad _workload;
	boolean fristRead = true;
	public MoniterThread(CounterStore counterStore,WorkLoad workload) {
		this._counterStore = counterStore;
		this._workload = workload;
	}

	@Override
	public void run() {
		try {
			String type = _workload.getConfig().getString(ConfigKey.SERVICE_TYPE, "ALL");
			if(PressureType.ALL.name().equalsIgnoreCase(type)){
				this.writeAndRead();
			}else if(PressureType.WRITE.name().equalsIgnoreCase(type)){
				this.write();
			}else if(PressureType.READ.name().equalsIgnoreCase(type)){
				this.read();
			}else if(PressureType.RREAD.name().equalsIgnoreCase(type)){
				this.randomRead();
			}else{
				throw new RuntimeException("service type config is error.");
			}
		} catch (Exception e) {
			log.debug("moniter thread error..",e);
		}
	}
	
	private void write(){
		String dbName = _counterStore.getDbname();
		int threadNumber = _counterStore.getThreadNumber();
		this.printInfo("wirte",_counterStore.write, dbName, threadNumber,_workload.getWriteStartTime());
	}
	
	
	private void randomRead(){
		String dbName = _counterStore.getDbname();
		int threadNumber = _counterStore.getThreadNumber();
		this.printInfo("rread",_counterStore.read, dbName, threadNumber,_workload.getReadStartTime());
	}
	
	private void read(){
		String dbName = _counterStore.getDbname();
		int threadNumber = _counterStore.getThreadNumber();
		this.printInfo("read",_counterStore.read, dbName, threadNumber,_workload.getReadStartTime());
	}
	
	private void writeAndRead(){
		try {
			String dbName = _counterStore.getDbname();
			int threadNumber = _counterStore.getThreadNumber();
			if(_workload.getReadStartTime()==0){
				this.printInfo("wirte",_counterStore.write, dbName, threadNumber,_workload.getWriteStartTime());
			}else{
				if(fristRead){
					this.printInfo("wirte",_counterStore.write, dbName, threadNumber,_workload.getWriteStartTime());
					fristRead = false;
				}
				this.printInfo("read",_counterStore.read, dbName, threadNumber,_workload.getReadStartTime());
			}
		} catch (Exception e) {
			log.debug("moniter thread error..",e);
		}
	}

	private void printInfo(String action,Current current, String dbName, int threadNumber,long startTime) {
		int success = current.getSuccess().get();
		int errors = current.getErrors().get();
		long latency = current.getLatency().get();
		long runtime = (System.currentTimeMillis() - startTime) / 1000;

		long tps = this.getTPS(threadNumber, success, latency, 0);
		if(tps==0){
			log.debug("error:: tps is 0");
			return ;
		}
		String tpsInfo = DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-ss HH:mm:ss")
				+" 测试服务[" + dbName + "]action["+action+"]线程数[" + threadNumber + "] 时间["
				+ runtime + "]秒 TPS[" + tps + "] success[" + success
				+ "] faild[" + errors + "]cost time["
				+ (double) (latency / success / Constans.NANO_MILLIS) + "]";
		log.info(tpsInfo);
		LogFileManager.getInstance().write(tpsInfo);
	}

	private long getTPS(long threadNum, long successNum, long totalTransMillis,
			long thinkTime) {
		if (successNum <= 0) {
			return 0;
		}
		return (long) (threadNum * 1_000 * Constans.NANO_MILLIS / ((double) totalTransMillis
				/ successNum + thinkTime * Constans.NANO_MILLIS));
	}
}
