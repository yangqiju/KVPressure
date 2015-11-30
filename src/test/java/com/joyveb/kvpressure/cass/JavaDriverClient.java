/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joyveb.kvpressure.cass;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.schemabuilder.Create;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;

public class JavaDriverClient {

	public String host;
	public int port;
	private Cluster cluster;
	private Session session;

	private static final ConcurrentMap<String, PreparedStatement> stmts = new ConcurrentHashMap<>();

	public JavaDriverClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public PreparedStatement prepare(String query) {
		PreparedStatement stmt = stmts.get(query);
		if (stmt != null)
			return stmt;
		synchronized (stmts) {
			stmt = stmts.get(query);
			if (stmt != null)
				return stmt;
			stmt = getSession().prepare(query);
			stmts.put(query, stmt);
		}
		return stmt;
	}

	public void connect(String keyspace) {

		PoolingOptions poolingOpts = new PoolingOptions()
				.setConnectionsPerHost(HostDistance.LOCAL, 8, 400)
				.setMaxRequestsPerConnection(HostDistance.LOCAL, 400);

		Cluster.Builder clusterBuilder = Cluster.builder()
				.addContactPoint(host).withPort(port)
				.withPoolingOptions(poolingOpts).withoutJMXReporting()
				.withoutMetrics(); // The driver uses metrics 3 with conflict
									// with our version
		cluster = clusterBuilder.build();
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s%n",
				metadata.getClusterName());
		for (Host host : metadata.getAllHosts()) {
			System.out.printf("Datatacenter: %s; Host: %s; Rack: %s%n",
					host.getDatacenter(), host.getAddress(), host.getRack());
		}
//		session = cluster.connect();
//		session.execute("DROP KEYSPACE  IF EXISTS joyveb;");
//		session.execute("CREATE KEYSPACE IF NOT EXISTS joyveb WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 1 };");
//		Create create = SchemaBuilder.createTable(keyspace, "pressure")
//				.ifNotExists().addPartitionKey("k", DataType.text())
//				.addColumn("v", DataType.text());
//		session.execute("use "+keyspace+";");
//		session.execute(create);
		session = cluster.connect(keyspace);
	}

	public Cluster getCluster() {
		return cluster;
	}

	public Session getSession() {
		return session;
	}

	public ResultSet execute(String query, ConsistencyLevel consistency) {
		SimpleStatement stmt = new SimpleStatement(query);
		stmt.setConsistencyLevel(from(consistency));
		return getSession().execute(stmt);
	}

	public ResultSet executePrepared(PreparedStatement stmt,
			List<Object> queryParams, ConsistencyLevel consistency) {

		stmt.setConsistencyLevel(from(consistency));
		BoundStatement bstmt = stmt.bind((Object[]) queryParams
				.toArray(new Object[queryParams.size()]));
		return getSession().execute(bstmt);
	}

	/**
	 * Get ConsistencyLevel from a C* ConsistencyLevel. This exists in the Java
	 * Driver ConsistencyLevel, but it is not public.
	 *
	 * @param cl
	 * @return
	 */
	public static ConsistencyLevel from(ConsistencyLevel cl) {
		switch (cl) {
		case ANY:
			return com.datastax.driver.core.ConsistencyLevel.ANY;
		case ONE:
			return com.datastax.driver.core.ConsistencyLevel.ONE;
		case TWO:
			return com.datastax.driver.core.ConsistencyLevel.TWO;
		case THREE:
			return com.datastax.driver.core.ConsistencyLevel.THREE;
		case QUORUM:
			return com.datastax.driver.core.ConsistencyLevel.QUORUM;
		case ALL:
			return com.datastax.driver.core.ConsistencyLevel.ALL;
		case LOCAL_QUORUM:
			return com.datastax.driver.core.ConsistencyLevel.LOCAL_QUORUM;
		case EACH_QUORUM:
			return com.datastax.driver.core.ConsistencyLevel.EACH_QUORUM;
		case LOCAL_ONE:
			return com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE;
		}
		throw new AssertionError();
	}

	public void disconnect() {
		cluster.close();
	}
}