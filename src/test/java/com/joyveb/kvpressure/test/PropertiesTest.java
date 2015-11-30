package com.joyveb.kvpressure.test;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

import com.joyveb.kvpressure.common.ConfigKey;

@Slf4j
public class PropertiesTest {

	@Test
	public void test() throws ConfigurationException{
		PropertiesConfiguration config = new PropertiesConfiguration("config.properties");
		String[] ips = config.getStringArray(ConfigKey.SERVICE_IPS);
		log.info("start test :"+Arrays.toString(ips));
	}
}
