package com.joyveb.kvpressure.pressure;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;

public class CassandraPressure extends BasePressure {

	private Cluster cluster;
	private Session session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PoolingOptions poolingOpts = new PoolingOptions()
        .setConnectionsPerHost(HostDistance.LOCAL, 8, 8)
        .setMaxRequestsPerConnection(HostDistance.LOCAL, 128)
        .setNewConnectionThreshold(HostDistance.LOCAL, 100);
		List<InetSocketAddress> list = new ArrayList<>();
		for (String host : hosts) {
			list.add(new InetSocketAddress(host, 9042));
		}
		cluster = Cluster
				.builder().withPoolingOptions(poolingOpts)
				.addContactPoints(hosts)
//				.withLoadBalancingPolicy(new WhiteListPolicy(new DCAwareRoundRobinPolicy(), list))
				.withoutMetrics().build();
		session = cluster.connect();
		Create create = SchemaBuilder.createTable("joyveb", "pressure")
				.ifNotExists().addPartitionKey("k", DataType.text())
				.addColumn("v", DataType.text());
		session.execute("CREATE KEYSPACE IF NOT EXISTS joyveb WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3 };");
		session.execute("use joyveb;");
		session.execute(create);
	}

	protected void setDown() {
		super.setDown();
		if (session != null) {
			session.close();
		}
		if (cluster != null) {
			cluster.close();
		}
	}

	@Override
	protected Thread getPressureThread(int i) {
		return new CassandraThread(session, i);
	}

	private static class CassandraThread extends Thread {
		private Session session;
		private String name;

		public CassandraThread(Session session, int i) {
			this.session = session;
			this.name = "CassandraThread_" + i;
		}

		@Override
		public void run() {
			long start = 0;
			int number = 0;
			while (!BasePressure.STOP) {
				try {
					start = System.currentTimeMillis();
					if (RequestType.WRITE.name().equals(
							BasePressure.requestType.name())) {
						store(name + number++);
					} else if (RequestType.READ.name().equals(
							BasePressure.requestType.name())) {
						get(name + number++);
					} else if (RequestType.ALL.name().equals(
							BasePressure.requestType.name())) {
						store(name + number);
						get(name + number++);
					}
					BasePressure.successNum.addAndGet(1);
					BasePressure.totalTransMillis.addAndGet(System
							.currentTimeMillis() - start);
					BasePressure.requestNum.addAndGet(1);
					TimeUnit.MILLISECONDS.sleep(BasePressure.thinkTime);
				} catch (Exception e) {
					e.printStackTrace();
					BasePressure.faildNum.addAndGet(1);
				}
			}
		}

		private void store(String key) {
			PreparedStatement ps = session
					.prepare("insert into joyveb.pressure (k,v) values(?,?)");
			ResultSet insertResult = session.execute(ps.bind(key,
					UUID.randomUUID().toString()).setConsistencyLevel(
					ConsistencyLevel.ONE));
			if (!insertResult.wasApplied()) {
				throw new RuntimeException("Not insert.");
			}
		}

		public void get(String key) {
			ResultSet rs = session.execute(session
					.prepare("select v from joyveb.pressure where k = ?")
					.bind(key).setConsistencyLevel(ConsistencyLevel.QUORUM));
			if (!rs.wasApplied()) {
				throw new RuntimeException("select not successed..");
			}
		}
	}
}
