package com.joyveb.kvpressure.client;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.cap.Quorum;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.google.common.collect.Lists;
import com.joyveb.kvpressure.common.ConfigKey;
import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.DBException;
import com.joyveb.kvpressure.core.ByteIterator;
import com.joyveb.kvpressure.core.StringByteIterator;

@Slf4j
public class RiakPressureClient extends Client {

	private RiakClient client = null;
	private RiakCluster cluster = null;
	private Namespace ns;
	private Quorum writeQuorum;
	private Quorum readQuorum;
	private int timeout;

	@Override
	public void init() throws DBException {
		try {
			PropertiesConfiguration props = getConfig();
			String namespace = props.getString("riak.namespace", "default");
			String table = props.getString("riak.table", "pressure");
			timeout = props.getInt("riak.timeout");
			String[] ips = props.getStringArray(ConfigKey.SERVICE_IPS);

			readQuorum = Quorum.oneQuorum();
			writeQuorum = Quorum.allQuorum();
			ns = new Namespace(namespace, table);

			RiakNode.Builder builder = new RiakNode.Builder()
					.withMinConnections(1).withMaxConnections(2);
			List<RiakNode> nodes = RiakNode.Builder.buildNodes(builder,
					Lists.newArrayList(ips));
			cluster = new RiakCluster.Builder(nodes).build();
			cluster.start();
			client = new RiakClient(cluster);
		} catch (Exception e) {
			log.info("riak client init error.", e);
			throw new DBException(e);
		}
	}

	@Override
	public void cleanup() throws DBException {
		if (cluster != null)
			cluster.shutdown();
	}

	@Override
	public int read(String table, String key, Set<String> fields,
			HashMap<String, ByteIterator> result) {
		try {
			Location location = new Location(ns, key);
			FetchValue fv = new FetchValue.Builder(location)
					.withOption(FetchValue.Option.R, readQuorum)
					.withOption(FetchValue.Option.TIMEOUT, timeout).build();
			FetchValue.Response response = client.execute(fv);
			// RiakObject obj = response.getValue(RiakObject.class);
			if (response.hasValues()) {
				return Constans.RESULT_OK;
			} else {
				return Constans.RESULT_ERROR;
			}
		} catch (Exception e) {
			log.debug("riak read is error..", e);
			return Constans.RESULT_ERROR;
		}
	}

	@Override
	public int insert(String table, String key,
			HashMap<String, ByteIterator> values) {
		try {
			Location location = new Location(ns, key);
			StoreValue store = new StoreValue.Builder(
					StringByteIterator.getStringMap(values))
					.withLocation(location)
					.withOption(StoreValue.Option.W, writeQuorum)
					.withOption(StoreValue.Option.TIMEOUT, timeout).build();
			client.execute(store);
			return Constans.RESULT_OK;
		} catch (Exception e) {
			log.debug("riak write is error..", e);
			return Constans.RESULT_ERROR;
		}
	}

}
