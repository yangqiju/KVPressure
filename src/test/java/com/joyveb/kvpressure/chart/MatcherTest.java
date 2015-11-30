package com.joyveb.kvpressure.chart;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherTest {

	public static void main(String[] args) {
		String str = "2015-11-12 15:07:12 测试服务[redis]action[wirte]线程数[100] 时间[11]秒 TPS[164901] success[1719012] faild[0]cost time[0.606421]";
		Pattern actionP = Pattern.compile("action\\[([a-z]+)\\]");
//		Pattern tpsP = Pattern.compile("TPS\\[([0-9]+)\\]");
//		Pattern timesP = Pattern.compile("time\\[([0-9]+\\.[0-9]+)\\]");
//		Pattern runTimeP = Pattern.compile("时间\\[([0-9]+)\\]");
		Matcher m = actionP.matcher(str);
		m.find();
		System.out.println(m.group(1));
//			System.out.println(m.group());
//			System.out.println(m.group(1));
	}
}
