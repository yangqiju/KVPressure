package com.joyveb.kvpressure.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class CounterStore {

	public @Getter @Setter String dbname;
	public @Getter @Setter int threadNumber;

	public Current write = new Current();
	public Current read = new Current();

	public @Data static class Current {
		AtomicInteger success = new AtomicInteger();
		AtomicInteger errors = new AtomicInteger();
		AtomicLong latency = new AtomicLong();
	}
}
