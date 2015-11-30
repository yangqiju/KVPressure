package com.joyveb.kvpressure.test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.joyveb.kvpressure.common.Constans;

public class CassandraTest {

	public static void main(String[] args) {
		Cluster cluster = Cluster
				.builder()
				.addContactPoints("172.16.7.82","172.16.7.83","172.16.7.85")
				.build();
		cluster.getConfiguration().getPoolingOptions()
				.setCoreConnectionsPerHost(HostDistance.LOCAL, 50)
				.setMaxConnectionsPerHost(HostDistance.LOCAL, 50);
		Session session = cluster.connect();
		Create create = SchemaBuilder.createTable("joyveb", "pressure")
				.ifNotExists().addPartitionKey("y_id", DataType.text());
		create.addColumn(Constans.FIELD_PERFIX + 0, DataType.text());
		try {
//			session.execute("DROP KEYSPACE  IF EXISTS joyveb;");
			session.execute("CREATE KEYSPACE IF NOT EXISTS  joyveb WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2};");
			session.execute("use joyveb;");
			session.execute(create);
//			ResultSet insertResult = session.execute(session.prepare(
//					"insert into joyveb.pressure (k,v) values(?,?)").bind("a",
//					UUID.randomUUID().toString()));
//			assert insertResult.wasApplied() == true;
//			ResultSet rs = session.execute(session.prepare(
//					"select v from joyveb.pressure where k = ?").bind("a"));
//			assert rs.wasApplied() == true;
		} finally {
			session.close();
			cluster.close();
		}
		System.out.println("end..");
	}
}
