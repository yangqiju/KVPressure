package com.joyveb.kvpressure.test;

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

public class RiakTest {

	public static void main(String[] args) {
		RiakNode.Builder builder = new RiakNode.Builder();
		builder.withMinConnections(10);
		builder.withMaxConnections(50);
		List<String> addresses = new ArrayList<String>();
		addresses.add("192.168.22.151");
		RiakClient client = null;
		RiakCluster cluster = null;
		try {
			List<RiakNode> nodes = RiakNode.Builder.buildNodes(builder,
					addresses);
			cluster = new RiakCluster.Builder(nodes).build();
			cluster.start();
			client = new RiakClient(cluster);
			Namespace ns = new Namespace("pressure", "pressure");
			Location location = new Location(ns, "my_key");
			RiakObject riakObject = new RiakObject();
			riakObject.setValue(BinaryValue.create("abc"));
			StoreValue store = new StoreValue.Builder(riakObject)
					.withLocation(location).withOption(Option.W, new Quorum(1))
					.build();
			client.execute(store);

			FetchValue fv = new FetchValue.Builder(location).build();
			FetchValue.Response response = client.execute(fv);
			RiakObject obj = response.getValue(RiakObject.class);
//			List<RiakObject> list = response.getValues();
//			for(RiakObject obj: list){
//				System.out.println(obj.getVClock());
//				System.out.println(obj.getValue().toString(Charset.defaultCharset()));
//			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (client != null)
				client.shutdown();
		}
	}
}
