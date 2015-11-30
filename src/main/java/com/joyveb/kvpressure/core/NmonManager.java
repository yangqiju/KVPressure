package com.joyveb.kvpressure.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NmonManager {

	public static void startNmon(String[] ips) {
		for (String ip : ips) {
			try {
				// 10seconde and 3600 times
				String cmd = "ssh root@" + ip
						+ "  mkdir -p /root/jkp/nmon && cd /root/jkp/nmon &&  nmon -f -s 10 -c 3600 -t";
				Process proccess = Runtime.getRuntime().exec(cmd);
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(proccess.getErrorStream()));
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					System.out.println(line);
				}
				bufferedReader.close();
			} catch (IOException e) {
				throw new RuntimeException("start nmon is error.",e);
			}
		}
	}
	
	public static void stopNmon(String[] ips){
		for (String ip : ips) {
			try {
				String cmd = "ssh root@" + ip
						+ "  pgrep -f nmon|xargs kill ";
				Process proccess = Runtime.getRuntime().exec(cmd);
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(proccess.getErrorStream()));
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					System.out.println(line);
				}
				bufferedReader.close();
			} catch (IOException e) {
				log.info("stop nmon is error {}",ip,e);
//				throw new RuntimeException("start nmon is error.",e);
			}
		}
	}
}
