package com.joyveb.kvpressure.common;

public class ConfigKey {

	public static final String TEST_NUMBER = "test.number";
	public static final String FIELD_NUMBER = "field.number";
	public static final String FIELD_LENGTH = "field.length";
	public static final String TABLE_NAME = "table.name";
	public static final String THREAD_NUMBER = "thread.number";
	public static final String SERVICE_NAME = "service.name";
	public static final String SERVICE_IPS = "service.ips";
	public static final String SERVICE_NMON = "service.nmon";
	public static final String SERVICE_TYPE = "service.type";

	/*------------------aerospike------------------*/
	public static final String AERO_NAMESPACE = "as.namespace";
	public static final String AERO_PORT = "as.port";
	public static final String AERO_TIMEOUT = "as.timeout";
	
	
	/*------------------cassandra------------------*/
	public static final String CASS_KEYSPACE = "cassandra.keyspace";
	public static final String CASS_KEYSPACE_REPLICA = "cassandra.replica";
	public static final String CASS_READ_CONSISTENCY= "cassandra.readconsistencylevel";
	public static final String CASS_WRITE_CONSISTENCY = "cassandra.writeconsistencylevel";

	/*------------------codis------------------*/
	public static final String CODIS_ZK_HOST = "codis.zkhost";
	public static final String CODIS_ZK_DIR = "codis.zkdir";

	/*------------------redis------------------*/
	public static final String REDIS_PORT = "redis.port";
	public static final String REDIS_PW = "redis.password";

	/*------------------jdbc------------------*/
	public static final String JDBC_DRIVER_CLASS = "db.driver";
	public static final String JDBC_CONNECTION_URL = "db.url";
	public static final String JDBC_CONNECTION_USER = "db.user";
	public static final String JDBC_CONNECTION_PASSWD = "db.passwd";
	public static final String JDBC_AUTO_COMMIT = "jdbc.autocommit";
	
	/*------------------ paldb.file ----------------------*/
	public static final String PALDB_FILE = "paldb.file";
}
