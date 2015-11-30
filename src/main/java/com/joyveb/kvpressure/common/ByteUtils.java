package com.joyveb.kvpressure.common;

import java.nio.ByteBuffer;

public class ByteUtils {

	public static byte[] intToBytes(int x) {
	    ByteBuffer buffer = ByteBuffer.allocate(5);
	    buffer.putInt(x);
	    return buffer.array();
	}

	public static int bytesToInt(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(5);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getInt();
	}
}
