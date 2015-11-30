package com.joyveb.kvpressure.start;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.joyveb.kvpressure.common.Client;
import com.joyveb.kvpressure.common.ClientFactory;
import com.joyveb.kvpressure.common.ConfigKey;
import com.joyveb.kvpressure.core.ClientThread;
import com.joyveb.kvpressure.core.CounterStore;
import com.joyveb.kvpressure.core.DBWrapper;
import com.joyveb.kvpressure.core.LogFileManager;
import com.joyveb.kvpressure.core.MoniterThread;
import com.joyveb.kvpressure.core.WorkLoad;
import com.joyveb.kvpressure.manager.PropertiesManager;

@Slf4j
public class PressureStarter {

	public static void main(String[] args)  {
		try {
			PropertiesConfiguration config = PropertiesManager.getInstance().getConfig();
			int threadNumber = config.getInt(ConfigKey.THREAD_NUMBER);

			WorkLoad workload = new WorkLoad();
			workload.init(config);
			CounterStore counterStore = new CounterStore();
			counterStore.setDbname(config.getString(ConfigKey.SERVICE_NAME));
			counterStore.setThreadNumber(threadNumber);
			
			List<ClientThread> clientThreads = new ArrayList<>();
			for (int i = 0; i < threadNumber; i++) {
				Client client = ClientFactory.getClientByName(config.getString(ConfigKey.SERVICE_NAME),i);
				client.setConfig(config);
				Client db = new DBWrapper(client, counterStore, config);
				ClientThread thread = new ClientThread(db, workload);
				thread.start();
				clientThreads.add(thread);
			}
			ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
			Thread moniter = new MoniterThread(counterStore, workload);
			service.scheduleAtFixedRate(moniter, 10, 10, TimeUnit.SECONDS);

			for (ClientThread thread : clientThreads) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			moniter.run();
			service.shutdown();
			log.info("结束任务");
			workload.clearup();
			ClientFactory.stopManager(config.getString(ConfigKey.SERVICE_NAME));
			LogFileManager.getInstance().close();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
