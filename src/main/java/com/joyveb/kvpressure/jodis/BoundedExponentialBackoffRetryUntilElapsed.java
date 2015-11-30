package com.joyveb.kvpressure.jodis;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;

public class BoundedExponentialBackoffRetryUntilElapsed implements RetryPolicy {

	private final int baseSleepTimeMs;

	private final int maxSleepTimeMs;

	private final long maxElapsedTimeMs;

	/**
	 * @param baseSleepTimeMs
	 *            initial amount of time to wait between retries
	 * @param maxSleepTimeMs
	 *            max time in ms to sleep on each retry
	 * @param maxElapsedTimeMs
	 *            total time in ms to retry
	 */
	public BoundedExponentialBackoffRetryUntilElapsed(int baseSleepTimeMs,
			int maxSleepTimeMs, long maxElapsedTimeMs) {
		this.baseSleepTimeMs = baseSleepTimeMs;
		this.maxSleepTimeMs = maxSleepTimeMs;
		if (maxElapsedTimeMs < 0) {
			this.maxElapsedTimeMs = Long.MAX_VALUE;
		} else {
			this.maxElapsedTimeMs = maxElapsedTimeMs;
		}
	}

	private long getSleepTimeMs(int retryCount, long elapsedTimeMs) {
		return Math.min(
				maxSleepTimeMs,
				(long) baseSleepTimeMs
						* Math.max(
								1,
								ThreadLocalRandom.current().nextInt(
										1 << Math.min(30, retryCount + 1))));
	}

	@Override
	public boolean allowRetry(int retryCount, long elapsedTimeMs,
			RetrySleeper sleeper) {
		if (elapsedTimeMs >= maxElapsedTimeMs) {
			return false;
		}
		long sleepTimeMs = Math.min(maxElapsedTimeMs - elapsedTimeMs,
				getSleepTimeMs(retryCount, elapsedTimeMs));
		try {
			sleeper.sleepFor(sleepTimeMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		return true;
	}
}
