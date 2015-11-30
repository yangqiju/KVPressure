package com.joyveb.kvpressure.pressure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.cap.Quorum;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.api.commands.kv.StoreValue.Option;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;

public class RiakPressure extends BasePressure {
	private RiakClient client = null;
	private RiakCluster cluster = null;
	Namespace ns = new Namespace("default", "pressure");

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		RiakNode.Builder builder = new RiakNode.Builder()
				.withMinConnections(10).withMaxConnections(200);
		List<String> addresses = new ArrayList<String>();
		for (String host : hosts) {
			addresses.add(host);
		}
		List<RiakNode> nodes = RiakNode.Builder.buildNodes(builder, addresses);
		cluster = new RiakCluster.Builder(nodes).build();
		cluster.start();
		// client = new RiakClient(cluster);
	}

	protected void setDown() {
		super.setDown();
		if (cluster != null) {
			cluster.shutdown();
		}
		if (client != null) {
			client.shutdown();
		}
		System.out.println("end..");
	}

	@Override
	protected Thread getPressureThread(int i) {
		return new RiakThread(new RiakClient(cluster), i);
	}

	public static class RiakThread extends Thread {
		private RiakClient client;
		private Namespace ns;
		private String name;

		public RiakThread(RiakClient client, int i) {
			this.client = client;
			this.ns = new Namespace("default", "pressure");
			this.name = "RiakThread_" + i;
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

		private void store(String key) throws Exception {
			Location location = new Location(ns, key);
			RiakObject riakObject = new RiakObject();
			riakObject.setValue(BinaryValue.create("my_value"));
			StoreValue store = new StoreValue.Builder(riakObject)
					.withLocation(location)
					.withOption(Option.W, new Quorum(-2)).build();
			client.execute(store);
		}

		private void get(String key) throws Exception {
			Location location = new Location(ns, key);
			FetchValue fv = new FetchValue.Builder(location).build();
			FetchValue.Response response = client.execute(fv);
			// RiakObject obj = response.getValue(RiakObject.class);
		}
	}

}
