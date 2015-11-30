package com.joyveb.kvpressure.core;

import java.io.File;
import java.io.PrintWriter;

import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogFileManager {

	private LogFileManager() {
	}

	private static class LFInstance {
		private static LogFileManager instance = new LogFileManager();
	}

	public static LogFileManager getInstance() {
		return LFInstance.instance;
	}

	private static final String FILE_NAME = "/TPS.txt";
	private static PrintWriter writer;

	static {
		String path = System.getProperty("user.dir" ).toString()+FILE_NAME;
		try {
			File file = new File(path);
			log.debug("log file path[{}]", path);
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			if (file.exists()) {
				if (!file.delete())
					throw new Exception(" delete file is error..[" + path + "]");
			}
			if (file.createNewFile()) {
				writer = new PrintWriter(file);
			} else {
				throw new Exception("create file is error[" + path + "]");
			}
		} catch (Exception e) {
			log.warn("init log writer is error..", e);
		}
		
	}

	public LogFileManager write(String context) {
		writer.write(context+"\n");
		writer.flush();
		return this;
	}

	@PreDestroy
	public void close() {
		if (writer != null) {
			writer.close();
			log.debug("writer destroy.. ");
		}
	}

}
