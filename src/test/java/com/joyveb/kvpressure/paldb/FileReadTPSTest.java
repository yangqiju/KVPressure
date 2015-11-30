package com.joyveb.kvpressure.paldb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.core.RandomByteIterator;

public class FileReadTPSTest {
	File file = new File("/home/yangqiju/tmp/store3.paldb");
	int requestNum = 10000000;

	@Test
	public void test() throws IOException{
//		this.writeFile();
		this.read();
//		this.read2();
	}
	
	public void writeFile() throws IOException{
		FileOutputStream output = new FileOutputStream(file);
		for(int i=0;i<requestNum;i++){
			output.write(new RandomByteIterator(25).toArray());
		}
		output.close();
	}
	
	public void read() throws IOException{
		try(RandomAccessFile mappedFile  = new RandomAccessFile(file, "r");
		FileChannel channel = mappedFile.getChannel();){
			MappedByteBuffer mbb  = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			
			long readStartTime = System.nanoTime();
			long trancost = 0;
			for(int i=0 ;i<requestNum;i++){
				mbb.position(RandomUtils.nextInt(requestNum-25));
				byte[] bytes = new byte[25];
				long costStart = System.nanoTime();
				mbb.get(bytes);
//				System.out.println(Arrays.toString(bytes));
				trancost = trancost + (System.nanoTime() - costStart);
			}
			long readEndTime = System.nanoTime() - readStartTime;
			System.out.println("read end.. cost time:" + readEndTime
					/ Constans.NANO_MILLIS);
			System.out.println("TPS:"
					+ (requestNum / ((readEndTime / Constans.NANO_MILLIS) / 1000)));
			System.out.println("cost time::" + (trancost / requestNum) + " nano");
			
//			read end.. cost time:1573.294757
//			TPS:6356088.047396957
//			cost time::112 nano
		}
	}
	public void read2() throws IOException{
		try(RandomAccessFile mappedFile  = new RandomAccessFile(file, "r");){
			long readStartTime = System.nanoTime();
			long trancost = 0;
			for(int i=0 ;i<requestNum;i++){
				mappedFile.seek(RandomUtils.nextInt(requestNum-25));
				byte[] bytes = new byte[25];
				long costStart = System.nanoTime();
				mappedFile.read(bytes);
				trancost = trancost + (System.nanoTime() - costStart);
			}
			long readEndTime = System.nanoTime() - readStartTime;
			System.out.println("read end.. cost time:" + readEndTime
					/ Constans.NANO_MILLIS);
			System.out.println("TPS:"
					+ (requestNum / ((readEndTime / Constans.NANO_MILLIS) / 1000)));
			System.out.println("cost time::" + (trancost / requestNum) + " nano");
			
//			read end.. cost time:6383.625601
//			TPS:1566507.9102435915
//			cost time::435 nano
		}
	}
}
