package com.joyveb.kvpressure.paldb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.joyveb.kvpressure.common.ByteUtils;

public class MulitThreadMMapTest {
	static File file = new File("/home/yangqiju/tmp/mulitThreadMmap.paldb");
	static int requestNum = 10000000;
	static int bytesSize = 5;

	@Test
	public void test() throws IOException{
		this.writeFile();
		this.read();
	}
	
	public void writeFile() throws IOException{
		FileOutputStream output = new FileOutputStream(file);
		for(int i=0;i<requestNum;i++){
			output.write(ByteUtils.intToBytes(i));
		}
		output.close();
	}
	
	public void read(){
		List<ReaderThread> threads = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			ReaderThread rt = new ReaderThread();
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
		System.out.println("read end..");
	}
	
	static class ReaderThread extends Thread{
		@Override
		public void run() {
			try(RandomAccessFile mappedFile  = new RandomAccessFile(file, "r");
					FileChannel channel = mappedFile.getChannel();){
						MappedByteBuffer mbb  = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
						for(int i=0 ;i<requestNum;i++){
							mbb.position(i*bytesSize);
							byte[] bytes = new byte[bytesSize];
							mbb.get(bytes);
							int number = ByteUtils.bytesToInt(bytes);
							if(i!=number){
								System.out.println(Thread.currentThread().getName());
								return;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
	}
	
}
