package com.lebin.server.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskExecutor {
	private static ExecutorService executorService;
	private static final Object LOCK=new Object();
	public static ExecutorService getExcuteService()
	{
		synchronized (LOCK) {
			if(executorService==null)
				executorService = Executors.newFixedThreadPool(Config.getInt("update_thread", 16));  
			return executorService;
		}
	}
}
