package com.joyveb.kvpressure.chart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChartUtils {

	public static void main(String[] args) throws IOException {
//		File file = new java.io.File("/home/yangqiju/work/installPackage/all/redis/redis_200_TPS.txt");
//		File file = new java.io.File("/home/yangqiju/work/installPackage/all/cassandra/cass_200_TPS.txt");
//		File file = new java.io.File("/home/yangqiju/work/installPackage/all/aerospike/aerospike_200_TPS.txt");
//		File file = new java.io.File("/home/yangqiju/work/installPackage/all/codis/codis_200_TPS.txt");
//		File file = new java.io.File("/home/yangqiju/work/installPackage/all/riak/riak_200_bitcask_TPS.txt");
//		File file = new java.io.File("/home/yangqiju/work/installPackage/all/riak/riak_200_mem_TPS.txt");
		File file = new java.io.File("/home/yangqiju/work/installPackage/all/paldb/paldb_100_TPS.txt");
		
		
		InputStreamReader read = new InputStreamReader(
				new FileInputStream(file));
		BufferedReader bufferedReader = new BufferedReader(read);
		String lineTxt;
		Pattern actionP = Pattern.compile("action\\[([a-z]+)\\]");
		Pattern tpsP = Pattern.compile("TPS\\[([0-9]+)\\]");
		Pattern timesP = Pattern.compile("time\\[([0-9]+\\.[0-9]+)\\]");
		Pattern runTimeP = Pattern.compile("时间\\[([0-9]+)\\]");
		System.out.println("action-time-costtime-TPS");
		while((lineTxt = bufferedReader.readLine()) != null){
			StringBuffer sb = new StringBuffer();
//			System.out.println(lineTxt);
			Matcher m = actionP.matcher(lineTxt);
			m.find();
			sb.append(m.group(1)).append("-");
			
			m = runTimeP.matcher(lineTxt);
			m.find();
			sb.append(m.group(1)).append("-");
			
			m = timesP.matcher(lineTxt);
			m.find();
			sb.append(m.group(1)).append("-");

			m = tpsP.matcher(lineTxt);
			m.find();
			sb.append(m.group(1));
			
			System.out.println(sb.toString());
		}
		bufferedReader.close();
	}
}
