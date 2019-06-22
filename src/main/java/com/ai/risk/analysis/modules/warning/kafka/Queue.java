package com.ai.risk.analysis.modules.warning.kafka;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Steven
 */
public class Queue {

	private static ArrayBlockingQueue<String> serviceQueue = new ArrayBlockingQueue(1000000);
	private static ArrayBlockingQueue<String> jdbcQueue = new ArrayBlockingQueue(1000000);

	public static final void offerService(String value) {
		serviceQueue.offer(value);
	}

	public static final void offerJdbc(String value) {
		jdbcQueue.offer(value);
	}

	public static final String pollService() {
		try {
			return serviceQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final String pollJdbc() {
		try {
			return jdbcQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

}
