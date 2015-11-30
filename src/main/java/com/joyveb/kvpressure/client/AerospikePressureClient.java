package com.joyveb.kvpressure.client;

import static com.joyveb.kvpressure.common.ConfigKey.AERO_NAMESPACE;
import static com.joyveb.kvpressure.common.ConfigKey.AERO_PORT;
import static com.joyveb.kvpressure.common.ConfigKey.AERO_TIMEOUT;
import static com.joyveb.kvpressure.common.ConfigKey.SERVICE_IPS;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Host;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.ConsistencyLevel;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.joyveb.kvpressure.common.Constans;
import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.DBException;
import com.joyveb.kvpressure.core.ByteArrayByteIterator;
import com.joyveb.kvpressure.core.ByteIterator;
@Slf4j
public class AerospikePressureClient extends Client {
	private static final int DEFAULT_PORT = 3000;
	private static final int DEFAULT_TIMEOUT = 10000;
	private static final String DEFAULT_NAMESPACE = "joyveb";

	private String namespace = null;
	private AerospikeClient client = null;

	private Policy readPolicy = new Policy();
	private WritePolicy insertPolicy = new WritePolicy();

	@Override
	public void init() throws DBException {
		insertPolicy.recordExistsAction = RecordExistsAction.CREATE_ONLY;
		PropertiesConfiguration props = getConfig();
		namespace = props.getString(AERO_NAMESPACE, DEFAULT_NAMESPACE);
		String[] ips = props.getStringArray(SERVICE_IPS);
		int port = props.getInt(AERO_PORT, DEFAULT_PORT);
		int timeout = props.getInt(AERO_TIMEOUT,DEFAULT_TIMEOUT);
		readPolicy.timeout = timeout;
		insertPolicy.timeout = timeout;
		insertPolicy.consistencyLevel = ConsistencyLevel.CONSISTENCY_ALL;

		ClientPolicy clientPolicy = new ClientPolicy();

		try {
			Host[] hosts = new Host[ips.length];
			for(int i = 0;i<ips.length;i++){
				hosts[i] = new Host(ips[i], port);
			}
			 client = new com.aerospike.client.AerospikeClient(clientPolicy, hosts);
		} catch (AerospikeException e) {
			log.error("init aerospike client is error.");
			throw new DBException(e);
		}
	}

	@Override
	public void cleanup() throws DBException {
		if(client!=null){
			client.close();
		}
	}

	@Override
	public int read(String table, String key, Set<String> fields,
			HashMap<String, ByteIterator> result) {
		try {
			Record record;

			if (fields != null) {
				record = client.get(readPolicy, new Key(namespace, table, key),
						fields.toArray(new String[fields.size()]));
			} else {
				record = client.get(readPolicy, new Key(namespace, table, key));
			}

			if (record == null) {
				log.debug("Record key " + key + " not found (read)");
				return Constans.RESULT_ERROR;
			}

			for (Map.Entry<String, Object> entry : record.bins.entrySet()) {
				result.put(entry.getKey(), new ByteArrayByteIterator(
						(byte[]) entry.getValue()));
			}

			return Constans.RESULT_OK;
		} catch (AerospikeException e) {
			log.debug("Error while reading key " + key + ": " + e);
			return Constans.RESULT_ERROR;
		}
	}

	private int write(String table, String key, WritePolicy writePolicy,
			HashMap<String, ByteIterator> values) {
		Bin[] bins = new Bin[values.size()];
		int index = 0;

		for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
			bins[index] = new Bin(entry.getKey(), entry.getValue().toArray());
			++index;
		}

		Key keyObj = new Key(namespace, table, key);

		try {
			client.put(writePolicy, keyObj, bins);
			return Constans.RESULT_OK;
		} catch (AerospikeException e) {
			log.debug("Error while writing key " + key + ": " + e);
			return Constans.RESULT_ERROR;
		}
	}

	@Override
	public int insert(String table, String key,
			HashMap<String, ByteIterator> values) {
		return write(table, key, insertPolicy, values);
	}

}
