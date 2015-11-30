package com.joyveb.kvpressure.riak;

import java.util.ArrayList;
import java.util.List;

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

public class RiakPressureClient {
	private RiakClient client = null;
	private RiakCluster cluster = null;
	Namespace ns = new Namespace("default", "pressure");

	public RiakPressureClient(String host) throws Exception {
		RiakNode.Builder builder = new RiakNode.Builder()
				.withMinConnections(10).withMaxConnections(200);
		List<String> addresses = new ArrayList<String>();
		addresses.add(host);
		List<RiakNode> nodes = RiakNode.Builder.buildNodes(builder, addresses);
		cluster = new RiakCluster.Builder(nodes).build();
		cluster.start();
		client = new RiakClient(cluster);
	}

	public void store(String key) throws Exception {
		Location location = new Location(ns, key);
		RiakObject riakObject = new RiakObject();
		riakObject.setValue(BinaryValue.create("my_value"));
		StoreValue store = new StoreValue.Builder(riakObject)
				.withLocation(location).withOption(Option.W, new Quorum(3))
				.build();
		client.execute(store);
	}

	public void get(String key) throws Exception {
		Location location = new Location(ns, key);
		FetchValue fv = new FetchValue.Builder(location).build();
		FetchValue.Response response = client.execute(fv);
		// RiakObject obj = response.getValue(RiakObject.class);
	}

	public void close() {
		if (client != null) {
			client.shutdown();
		}
	}
}
