package com.joyveb.kvpressure.test;

import java.nio.ByteBuffer;

import org.junit.Test;

public class Number16Test {

	@Test
	public void Test() {
		System.out.println(int2bytes(10000000).length);
		System.out.println(bytes2int(int2bytes(10000000)));
	}

	public static byte[] int2bytes(int i) {
		byte[] b = new byte[4];
		b[0] = (byte) (0xff & i);
		b[1] = (byte) ((0xff00 & i) >> 8);
		b[2] = (byte) ((0xff0000 & i) >> 16);
		b[3] = (byte) ((0xff000000 & i) >> 24);
		return b;
	}

	public static int bytes2int(byte[] bytes) {
		int num = bytes[0] & 0xFF;
		num |= ((bytes[1] << 8) & 0xFF00);
		num |= ((bytes[2] << 16) & 0xFF0000);
		num |= ((bytes[3] << 24) & 0xFF000000);
		return num;
	}
	
	@Test
	public void test2(){
		System.out.println(intToBytes(1).length);
	}
	
	public byte[] intToBytes(int x) {
	    ByteBuffer buffer = ByteBuffer.allocate(5);
	    buffer.putInt(x);
	    return buffer.array();
	}

	public int bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(5);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getInt();
	}
}
