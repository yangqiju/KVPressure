package com.joyveb.kvpressure.pressure;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BasePressure {

	public static AtomicLong responseNum = new AtomicLong();
	public static AtomicLong requestNum = new AtomicLong();
	public static AtomicLong successNum = new AtomicLong();
	public static AtomicLong faildNum = new AtomicLong();
	public static volatile boolean STOP = false;
	public static AtomicLong totalTransMillis = new AtomicLong();
	protected long _printLogTime = 30;
	public static long thinkTime = 0;
	protected long _threadNums = 0;
	protected long _runTime = 0;
	protected long _startTime = 0;
	private static PrintWriter writer;
	protected static RequestType requestType = RequestType.WRITE;
	protected String[] hosts = {};
	
	protected abstract Thread getPressureThread(int i);

	protected void setUp() throws Exception {
		this.initLogFile();
		this.initProperties();
	}

	protected void setDown() {
		if (writer != null) {
			writer.close();
		}
	}
	
	protected void initProperties(){
		this.hosts = PropertiesManager.getInstance().getHosts();
		this._threadNums=PropertiesManager.getInstance().getThreadNums();
		this._runTime = PropertiesManager.getInstance().getRunTime();
		BasePressure.requestType = PropertiesManager.getInstance().getRequestType();
		BasePressure.thinkTime = PropertiesManager.getInstance().getThinkTime();
	}


	private void initLogFile() {
		String path = System.getProperty("user.dir").toString() + "/TPS.txt";
		try {
			File file = new File(path);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (file.exists()) {
				file =  new File(path+System.currentTimeMillis());
//				if (!file.delete())
//					throw new Exception(" delete file is error..[" + path + "]");
			}
			if (file.createNewFile()) {
				writer = new PrintWriter(file);
				System.out.println("create file success:" + file.getAbsolutePath());
			} else {
				throw new Exception("create file is error[" + file.getAbsolutePath() + "]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeLog(String text){
		writer.write(text);
		writer.flush();
	}

	protected void printInfo() {
		long tps = this.getTPS(this._threadNums, successNum.get(),
				totalTransMillis.get(), thinkTime);
		long time = (System.currentTimeMillis() - this._startTime) / 1000;
		String tpsInfo = "线程数[" + this._threadNums + "] 时间[" + time + "] TPS["
				+ tps + "] success[" + successNum.get() + "] faild["
				+ faildNum.get() + "]cost time["
				+ (double) (totalTransMillis.get() / successNum.get()/1_000_000.0) + "]";
		System.out.println(tpsInfo);
		this.writeLog(time+" "+tps+"\n");
	}

	protected void start() throws Exception {
		this.setUp();
		ExecutorService es = Executors.newFixedThreadPool((int)_threadNums);
		_startTime = System.currentTimeMillis();
		for (int i = 0; i < this._threadNums; i++) {
			es.submit(getPressureThread(i));
		}
		while (true) {
			if (System.currentTimeMillis() > (_startTime + this._runTime * 1000)) {
				break;
			}
			TimeUnit.SECONDS.sleep(this._printLogTime);
			this.printInfo();
		}
		STOP = true;
		es.shutdownNow();
		this.setDown();
		this.printInfo();
	}

	protected long getTPS(long threadNum, long successNum,
			long totalTransMillis, long thinkTime) {
		if (successNum <= 0) {
			return 0;
		}
		return (long) (threadNum * 1_000*1_000_000 / ((double) totalTransMillis / successNum + thinkTime*1_000_000));
	}
}
