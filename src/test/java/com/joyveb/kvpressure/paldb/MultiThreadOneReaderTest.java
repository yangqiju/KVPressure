package com.joyveb.kvpressure.paldb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;
import com.linkedin.paldb.api.StoreWriter;

public class MultiThreadOneReaderTest {
	static File file = new File("/home/yangqiju/tmp/store4.paldb");
	static int dataSize = 10000000;
	public static final double NANO_MILLIS = 1000000.0;
	public static void main(String[] args) throws IOException {
//		write();
		read();
	}
	
	public static  void write() throws IOException {
		StoreWriter writer = PalDB.createWriter(file);
		long writeStartTime = System.nanoTime();
		for (int i = 0; i < dataSize; i++) {
			writer.put("key"+i,"value"+i);
		}
		writer.close();
		long writeEndTime = System.nanoTime() - writeStartTime;
		System.out.println("write end..cost time:"
				+ (writeEndTime / NANO_MILLIS));
		System.out.println("all write is end..");
	}
	
	public static void read(){
		List<ReaderThread> threads = new ArrayList<ReaderThread>();
		StoreReader reader = PalDB.createReader(file);
		
		for (int i = 0; i < 4; i++) {
			ReaderThread rt = new ReaderThread(reader);
			threads.add(rt);
			rt.start();
		}
		for(ReaderThread rt : threads){
			try {
				rt.join();
			} catch (InterruptedException e) {
				rt.interrupt();
			}
		}
		
		reader.close();
	}
	
	static class ReaderThread extends Thread{
		StoreReader reader;
		public ReaderThread(StoreReader reader) {
			this.reader = reader;
		}
		@Override
		public void run() {
			for (int i = 0; i < dataSize; i++) {
				String result = reader.get("key"+i);
				if(!("value"+i).equals(result)){
					System.out.println("value is not equal:"+result);
					System.out.println(Thread.currentThread().getName());
					return;
				}
			}
		}
	}
}
