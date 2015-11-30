package com.joyveb.kvpressure.cass;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;

public class CassTask extends Thread {

	private JavaDriverClient client;

	public CassTask(JavaDriverClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		PreparedStatement insertPS = client.getSession().prepare(
				"insert into joyveb.pressure (y_id,field0) values(?,?)");
		PreparedStatement queryPS = client.getSession().prepare(
				"select field0 from joyveb.pressure where y_id = ?");
		int key = 0;
		long start = System.currentTimeMillis();
		while (!CassStarter.STOP) {
			try {
				start = System.currentTimeMillis();
				client.getSession().execute(
						insertPS.bind(this.getName() + key++, "abcdedfafdsfd")
								.setConsistencyLevel(ConsistencyLevel.QUORUM));

//				client.getSession().execute(
//						queryPS.bind(this.getName() + key++).setConsistencyLevel(
//								ConsistencyLevel.QUORUM));

				CassStarter.successNum.addAndGet(1);
				CassStarter.totalTransMillis.addAndGet(System
						.currentTimeMillis() - start);
				CassStarter.requestNum.addAndGet(1);
			} catch (Exception e) {
				e.printStackTrace();
				CassStarter.faildNum.addAndGet(1);
			}
		}
	}
}
