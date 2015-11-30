package com.joyveb.kvpressure.aerospike;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.policy.WritePolicy;

public class AerospikeTest {

	public static void main(String[] args) {
		WritePolicy policy = new WritePolicy();
		policy.timeout = 50;  // 50 millisecond timeout.
		AerospikeClient client = new AerospikeClient(new ClientPolicy(), "192.168.22.151", 3000);
		Key key = new Key("bar", null, "mykey");
		Bin bin = new Bin("mybin", "myvalue");
//		client.put(policy, key, bin);
		
		Record record = client.get(new QueryPolicy(), key, "mybin");
		System.out.println(record);
		client.close();
	}
}
